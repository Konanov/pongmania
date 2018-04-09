package com.konanov.player.endpoint;

import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.player.model.Player;
import com.konanov.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlayerEndpoint {

    private final PlayerService service;

    @GetMapping(path = "player/all")
    public Flux<Player> retrieveAll() {
        return service.retrieveAll();
    }

    @GetMapping(path = "player/{email}/has/league")
    public Mono<Boolean> playerHasLeague(@PathVariable String email) {
        return service.playerHasLeague(email);
    }

    @GetMapping(path = "player/{email}/public/league")
    public Mono<PublicLeague> playersPublicLeague(@PathVariable String email) {
        return service.playersPublicLeague(email);
    }

    @GetMapping(path = "players/of/{type}/league")
    public Flux<Player> playersOfLeague(@PathVariable String type) {
        Optional<PublicLeagueType> value = PublicLeagueType.leagueByName(type);
        if (value.isPresent()) {
            return service.getPlayersOfLeague(value.get());
        } else {
            return Flux.empty();
        }
    }
}
