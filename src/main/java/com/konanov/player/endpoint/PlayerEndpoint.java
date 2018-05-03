package com.konanov.player.endpoint;

import com.konanov.game.service.GameService;
import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.player.model.Player;
import com.konanov.player.service.PlayerService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlayerEndpoint {

  private final PlayerService playerService;
  private final GameService gameService;

  @GetMapping(path = "player/all")
  public Flux<Player> retrieveAll() {
    return playerService.retrieveAll();
  }

  @GetMapping(path = "player/{email}/has/league")
  public Mono<Boolean> playerHasLeague(@PathVariable String email) {
    return playerService.playerHasLeague(email);
  }

  @GetMapping(path = "player/{email}/public/league")
  public Mono<PublicLeague> playersPublicLeague(@PathVariable String email) {
    return playerService.playersPublicLeague(email);
  }

  @GetMapping(path = "players/{email}")
  public Mono<Player> getPlayer(@PathVariable String email) {
    return playerService.findByEmail(email);
  }

  @GetMapping(path = "players/of/{name}/league")
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
