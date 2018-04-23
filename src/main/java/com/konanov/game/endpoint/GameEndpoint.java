package com.konanov.game.endpoint;

import com.konanov.rating.model.Rating;
import com.konanov.game.model.Game;
import com.konanov.game.model.Match;
import com.konanov.statistics.model.Statistic;
import com.konanov.rating.service.RatingCalculationService;
import com.konanov.statistics.service.StatisticsCalculatingService;
import com.konanov.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.ArrayList;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GameEndpoint {

    private final GameService gameService;
    private final RatingCalculationService ratingCalculationService;
    private final StatisticsCalculatingService statisticsCalculatingService;

    private static final String APP_HOST_PORT = "http://localhost:8080";

    /**
     * Offer new {@link Game} to player.
     * Game created without any {@link Match} present.
     * {@link Game} timestamp should be in future, as game can only be planned. You can not offer {@link Game}
     * that was held in past.
     * */
    @PostMapping("/game/offer")
    public Mono<ResponseEntity<String>> offerGame(@RequestBody Game game) {
        return gameService.insert(game).flatMap(this::getObjectResponseEntity);
    }

    /**
     * Add {@link Match} to existing game. {@link Match} should contain scores for
     * {@literal Host} and {@literal Guest} players of the game.
     * */
    @PostMapping("/game/{uuid}/addMatch")
    public Mono<ResponseEntity<String>> addMatch(@PathVariable String uuid, @RequestBody Match match) {
        return gameService.findById(new ObjectId(uuid))
                .publishOn(Schedulers.elastic())
                .map(this::initializeMatches)
                .map(it -> addMatch(match, it))
                .flatMap(gameService::save)
                .flatMap(this::getObjectResponseEntity);
    }

    /**
     * Calculate {@link Game} results and save new {@link Rating} and {@link Statistic} for both players.
     * */
    @PostMapping("/game/{uuid}/calculate")
    public Flux<Rating> calculateGame(@PathVariable String uuid) {
        return gameService.findById(new ObjectId(uuid))
                .doOnNext(statisticsCalculatingService::calculate)
                .flatMapMany(ratingCalculationService::calculate);
    }

    @GetMapping("/game/{uuid}/all")
    public Flux<Game> getUserGames(@PathVariable String uuid) {
        return gameService.findAllUserGames(new ObjectId(uuid));
    }

    private Mono<ResponseEntity<String>> getObjectResponseEntity(Game registered) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/game/")
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return Mono.just(ResponseEntity.created(location).build());
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