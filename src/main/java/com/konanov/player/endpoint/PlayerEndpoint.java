package com.konanov.player.endpoint;

import com.konanov.game.service.GameService;
import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.player.model.Player;
import com.konanov.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static com.konanov.player.endpoint.PlayerEndpoint.PLAYER_URL;

@Slf4j
@RestController
@RequestMapping(PLAYER_URL)
@RequiredArgsConstructor
public class PlayerEndpoint {

  static final String PLAYER_URL = "/player";

  private final PlayerService playerService;
  private final GameService gameService;

  @GetMapping(path = "/all")
  public Flux<Player> retrieveAll() {
    return playerService.retrieveAll();
  }

  @GetMapping(path = "/{email}/has/league")
  public Mono<Boolean> playerHasLeague(@PathVariable String email) {
    return playerService.playerHasLeague(email);
  }

  @GetMapping(path = "/{email}/public/league")
  public Mono<PublicLeague> playersPublicLeague(@PathVariable String email) {
    return playerService.playersPublicLeague(email);
  }

  @GetMapping(path = "/byEmail/{email}")
  public Mono<Player> getPlayerByEmail(@PathVariable String email) {
    return playerService.findByEmail(email);
  }

  @GetMapping(path = "/byId/{id}")
  public Mono<Player> getPlayerById(@PathVariable String id) {
    return playerService.findById(new ObjectId(id));
  }

  @GetMapping(path = "/of/{name}/league")
  public Flux<Player> playersOfLeague(@PathVariable String name) {
    return PublicLeagueType
        .leagueByName(name)
        .flatMapMany(playerService::getPlayersOfLeague)
        .flatMap(this::setStatistics);
  }

  private Mono<Player> setStatistics(Player player) {
    return gameService.matchWinRatio(player.getId())
        .map(winRatio -> winRatio.get(player.getId()))
        .map(player::setMatchWinRatio)
        .then(gameService.countPlayedGames(player.getId())
            .map(counts -> counts.get(player.getId()))
            .map(player::setPlayedGamesCount));
  }

  private void checkGamesNotNull(Player player) {
    if (player.getGames() == null || player.getGames().isEmpty()) {
      player.setGames(new ArrayList<>());
    }
  }
}
