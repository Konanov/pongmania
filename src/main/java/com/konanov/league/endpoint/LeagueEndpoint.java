package com.konanov.league.endpoint;

import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.player.model.Player;
import com.konanov.league.service.LeagueService;
import com.konanov.exceptions.PongManiaException;
import com.konanov.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LeagueEndpoint {

    private final PlayerService playerService;
    private final LeagueService leagueService;
    private static final String LEAGUE_NOT_ASSIGNED = "League was not assigned to player: %s";
    private static final String APP_HOST_PORT = "http://localhost:8080";

    @PostMapping(path = "league/{type}/assign")
    public Mono<PublicLeague> assignPublicLeague(@PathVariable String type,
                                                 @RequestBody Player.Credentials credentials) {
        Optional<PublicLeagueType> value = PublicLeagueType.leagueByName(type);
        if (value.isPresent()) {
            Mono<Player> player = playerService.findByEmail(credentials.getEmail());
            Mono<PublicLeague> league = leagueService.findByType(value.get());
            return Mono.zip(player, league, (p, l) -> {
                p.setPublicLeague(l);
                return p;
            }).flatMap(playerService::insert)
                    .map(Player::getPublicLeague)
                    .doOnError((e) -> new PongManiaException(String.format(LEAGUE_NOT_ASSIGNED, e.getMessage())));
        } else {
            return Mono.empty();
        }
    }

    private ResponseEntity<String> getObjectResponseEntity(PublicLeague league) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/league/")
                .path("/{id}")
                .buildAndExpand(league.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
