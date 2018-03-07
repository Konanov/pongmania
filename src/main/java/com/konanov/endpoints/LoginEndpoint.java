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

import java.net.URI;
import java.util.Optional;

//import com.konanov.service.SecurityService;

@RestController
@RequiredArgsConstructor
public class LoginEndpoint {

    private final BCryptPasswordEncoder encoder;
    private final PlayerRepository playerRepository;
    //private final SecurityService securityService;

    @PostMapping(path = "registration")
    public ResponseEntity<Object> registration(@RequestBody Player.Credentials credentials) {
        Optional<Player> player = playerRepository.findByCredentials_Email(credentials.getEmail());
        if (player.isPresent()) {
            throw new PongManiaException(String.format("User with email: %s already exists", credentials.getEmail()));
        }
        credentials.setPassword(encoder.encode(credentials.getPassword()));
        Player registered = playerRepository.insert(new Player(new ObjectId(), credentials, new String[]{"ROLE_USER"}));
        //securityService.autoLogin(credentials.getEmail(), credentials.getPassword());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
