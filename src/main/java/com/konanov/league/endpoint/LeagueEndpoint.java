package com.konanov.league.endpoint;

import com.konanov.exceptions.PongManiaException;
import com.konanov.league.model.PrivateLeague;
import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.league.service.LeagueService;
import com.konanov.player.model.Player;
import com.konanov.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LeagueEndpoint {

    private final PlayerService playerService;
    private final LeagueService leagueService;
    private static final String LEAGUE_NOT_ASSIGNED = "League was not assigned to player: %s";

    @PostMapping(path = "league/{type}/assign")
    public Mono<PublicLeague> assignPublicLeague(@PathVariable String type,
                                                 @RequestBody Player.Credentials credentials) {
        return PublicLeagueType
                .leagueByName(type)
                .flatMap(name -> findLeague(credentials, name))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<PublicLeague> findLeague(@RequestBody Player.Credentials credentials, PublicLeagueType name) {
        Mono<PublicLeague> leagueMono = leagueService.findByType(name);
        Mono<Player> playerMono = playerService.findByEmail(credentials.getEmail());
        return Mono.zip(playerMono, leagueMono, Player::setPublicLeague)
                   .flatMap(playerService::insert)
                   .map(Player::getPublicLeague)
                   .doOnError((e) -> new PongManiaException(format(LEAGUE_NOT_ASSIGNED, e.getMessage())));
    }

    @PostMapping(path = "league/private/{name}")
    public Mono<PrivateLeague> createPrivateLeague(@PathVariable String name) {
        return leagueService.createPrivateLeague(name);
    }

    @PostMapping(path = "league/private/{name}/add")
    public Mono<ResponseEntity> addPlayer(@PathVariable String name, @RequestBody Player.Credentials credentials) {
        //FIXME: add error handling
        return leagueService
                .findPrivateLeagueByName(name)
                .flatMap(league -> {
                    league
                            .getPlayers()
                            .add(credentials);
                    return leagueService.saveLeague(league);
                })
                .map((league) -> new ResponseEntity(HttpStatus.OK));
    }

    @PostMapping(path = "league/private/{name}/remove")
    public Mono<ResponseEntity> removePlayer(@PathVariable String name, @RequestBody Player.Credentials credentials) {
        //FIXME: add error handling
        return leagueService
                .findPrivateLeagueByName(name)
                .flatMap(league -> {
                    league
                            .getPlayers()
                            .remove(credentials);
                    return leagueService.saveLeague(league);
                })
                .map((league) -> new ResponseEntity(HttpStatus.OK));
    }
}
