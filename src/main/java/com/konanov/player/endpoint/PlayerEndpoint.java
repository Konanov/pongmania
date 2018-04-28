package com.konanov.player.endpoint;

import com.konanov.game.model.Game;
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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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

    @GetMapping(path = "players/of/{type}/league")
    public Flux<Player> playersOfLeague(@PathVariable String type) {
        Optional<PublicLeagueType> value = PublicLeagueType.leagueByName(type);
        if (value.isPresent()) {
            Flux<Player> players = playerService.getPlayersOfLeague(value.get());
            Flux<ObjectId> ids = players.map(Player::getId);
            Flux<Game> playerGames = ids.flatMap(gameService::findAllUserGames);
            return Flux.zip(players, playerGames).map(
                pair -> {
                    Player player = pair.getT1();
                    Game game = pair.getT2();
                    checkGamesNotNull(player);
                    if (player.getId() == game.getHostId() || player.getId() == game.getGuestId()) {
                        player.getGames().add(game);
                        return player;
                    }
                    checkGamesNotNull(player);
                    return player;
                }
            );
        } else {
            return Flux.empty();
        }
    }

    private void checkGamesNotNull(Player player) {
        if (player.getGames() == null || player.getGames().isEmpty()) {
            player.setGames(new ArrayList<>());
        }
    }
}
