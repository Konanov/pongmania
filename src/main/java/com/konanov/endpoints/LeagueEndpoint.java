package com.konanov.endpoints;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.repository.PublicLeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LeagueEndpoint {

    private final PlayerRepository repository;
    private final PublicLeagueRepository leagueRepository;
    private static final String APP_HOST_PORT = "http://localhost:8080";

    @PostMapping(path = "league/{type}/assign")
    public Mono<ResponseEntity<String>> assignPublicLeague(@PathVariable PublicLeagueType type,
                                                           @RequestBody Player.Credentials credentials) {
        Mono<Player> player = repository.findByCredentials_Email(credentials.getEmail());
        Mono<PublicLeague> league = leagueRepository.findByType(type);
        return Mono.zip(player, league, (p, l) -> {
            p.setPublicLeague(l);
            repository.save(p);
            return l;
        }).map(this::getObjectResponseEntity);
    }

    private ResponseEntity<String> getObjectResponseEntity(PublicLeague league) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/league/")
                .path("/{id}")
                .buildAndExpand(league.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
