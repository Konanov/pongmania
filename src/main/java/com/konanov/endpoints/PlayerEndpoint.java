package com.konanov.endpoints;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlayerEndpoint {

    private final PlayerRepository repository;

    @PostMapping(path = "player/create")
    public Player createPlayer(@RequestBody Player player) {
        final Player.Credentials credentials = player.getCredentials();
        //log.info("Request to create new Player: {} {}", credentials.getName(), credentials.getSurname());
        return repository.insert(player);
    }
}
