package com.konanov.endpoints;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlayerEndpoint {

    private final PlayerRepository repository;

    @PostMapping(path = "player/create")
    public Flux<Player> createPlayer(@RequestBody Mono<Player> player) {
        return repository.insert(player);
    }

    @GetMapping(path = "player/all")
    public Flux<Player> retrieveAll() {
        return repository.findAll();
    }

    @GetMapping(path = "player/{email}/has/league")
    public Mono<Boolean> playerHasLeague(@PathVariable String email) {
        return repository.findByCredentials_Email(email)
                .flatMap(player -> Mono.just(player.getPublicLeague() != null));
    }
}
