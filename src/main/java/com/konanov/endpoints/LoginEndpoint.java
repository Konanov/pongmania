package com.konanov.endpoints;

import com.konanov.model.person.Player;
import com.konanov.model.person.Role;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.SecurityService;
import com.konanov.service.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static com.konanov.model.person.Role.Name.USER;
import static java.util.Collections.singleton;

@RestController
@RequiredArgsConstructor
public class LoginEndpoint {

    private final PlayerRepository playerRepository;
    private final SecurityService securityService;

    @PostMapping(path = "registration")
    public ResponseEntity<Object> registration(@RequestBody Player.Credentials credentials) {
        Optional<Player> player = playerRepository.findByCredentials_Email(credentials.getEmail());
        if (player.isPresent()) {
            throw new PongManiaException(String.format("User with email: %s already exists", credentials.getEmail()));
        }
        credentials.setRoles(singleton(new Role(USER)));
        Player registered = playerRepository.insert(new Player(new ObjectId(), credentials));
        securityService.autoLogin(credentials.getEmail(), credentials.getPassword());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
