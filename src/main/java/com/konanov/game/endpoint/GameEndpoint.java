package com.konanov.game.endpoint;

import static java.time.ZonedDateTime.now;

import com.konanov.game.model.Game;
import com.konanov.game.model.Match;
import com.konanov.game.repository.GameRepository;
import com.konanov.game.service.GameService;
import com.konanov.player.service.PlayerService;
import com.konanov.rating.model.Rating;
import com.konanov.rating.service.RatingCalculationService;
import com.konanov.statistics.model.Statistic;
import com.konanov.statistics.service.StatisticsCalculatingService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GameEndpoint {

  private final GameService gameService;
  private final GameRepository gameRepository;
  private final PlayerService playerService;
  private final RatingCalculationService ratingCalculationService;
  private final StatisticsCalculatingService statisticsCalculatingService;

  /**
   * Offer new {@link Game} to player.
   * Game created without any {@link Match} present.
   * {@link Game} timestamp should be in future, as game can only be planned. You can not offer {@link Game}
   * that was held in past.
   */
  @PostMapping("/game/offer")
  public Mono<Game> offerGame(@RequestBody Game game) {
    playerService.findByEmail(game.getHostEmail())
        .cache()
        .repeat()
        .subscribe(it -> game.setHostId(it.getId()));
    playerService.findByEmail(game.getGuestEmail())
        .cache()
        .repeat()
        .subscribe(it -> game.setGuestId(it.getId()));
    game.setCreatedAt(now());
    return gameRepository.insert(game);
  }

  /**
   * Add {@link Match} to existing game. {@link Match} should contain scores for
   * {@literal Host} and {@literal Guest} players of the game.
   */
  @PostMapping("/game/{uuid}/addMatch")
  public Mono<Game> addMatch(@PathVariable String uuid, @RequestBody Match match) {
    return gameRepository.findById(new ObjectId(uuid))
        .publishOn(Schedulers.elastic())
        .map(this::initializeMatches)
        .map(it -> addMatch(match, it))
        .flatMap(gameRepository::save);
  }

  /**
   * Calculate {@link Game} results and save new {@link Rating} and {@link Statistic} for both players.
   */
  @PostMapping("/game/{uuid}/calculate")
  public Flux<Rating> calculateGame(@PathVariable String uuid) {
    return gameRepository.findById(new ObjectId(uuid))
        .doOnNext(statisticsCalculatingService::calculate)
        .flatMapMany(ratingCalculationService::calculate);
  }

  @GetMapping("/game/{uuid}/all")
  public Flux<Game> getUserGames(@PathVariable String uuid) {
    return gameService.findAllPlayerGames(new ObjectId(uuid));
  }

  private Game addMatch(@RequestBody Match match, Game it) {
    it.getMatches().add(match);
    return it;
  }

  private Game initializeMatches(Game it) {
    if (it.getMatches() == null) {
      it.setMatches(new ArrayList<>());
    }
    return it;
  }
}
