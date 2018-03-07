package com.konanov.endpoints;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlayerEndpoint {

    private final PlayerRepository repository;

    @PostMapping(path = "player/create")
    public Player createPlayer(@RequestBody Player player) {
        //log.info("Request to create new Player: {} {}", credentials.getName(), credentials.getSurname());
        return repository.insert(player);
    }

    @GetMapping(path = "player/all")
    public Collection<Player> retrieveAll() {
        return repository.findAll();
    }
}
