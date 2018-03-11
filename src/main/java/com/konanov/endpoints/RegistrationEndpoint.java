package com.konanov.endpoints;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RegistrationEndpoint {

    private static final String USER_EXISTS = "User with email: %s already exists";
    private static final String CAN_NOT_PERSIST = "Could not save Player to database \nException: %s";
    private static final String APP_HOST_PORT = "http://localhost:8080";

    private final BCryptPasswordEncoder encoder;
    private final PlayerRepository playerRepository;

    @PostMapping(path = "registration")
    public Mono<ResponseEntity<Object>> registration(@RequestBody Player.Credentials credentials) {
        playerRepository.findByCredentials_Email(credentials.getEmail())
                .doOnSuccess((it) -> {
                    throw new PongManiaException(String.format(USER_EXISTS, credentials.getEmail()));
                });
        credentials.setPassword(encoder.encode(credentials.getPassword()));
        return playerRepository.insert(new Player(new ObjectId(), credentials, new String[]{"ROLE_USER"}))
                .map(this::getObjectResponseEntity)
                .doOnError((e) -> new PongManiaException(String.format(CAN_NOT_PERSIST, e.getMessage())));
    }

    private ResponseEntity<Object> getObjectResponseEntity(Player registered) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/player/")
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
