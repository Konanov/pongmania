package com.konanov.league.endpoint;

import com.konanov.exceptions.PongManiaException;
import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import com.konanov.league.service.LeagueService;
import com.konanov.player.model.Player;
import com.konanov.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
}
