package com.konanov.endpoints;

import com.konanov.gliko.Rating;
import com.konanov.gliko.RatingCalculator;
import com.konanov.model.person.Player;
import com.konanov.repository.RatingRepository;
import com.konanov.service.exceptions.PongManiaException;
import com.konanov.service.model.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static java.time.LocalDateTime.now;


@Slf4j
@RestController
@RequiredArgsConstructor
public class RegistrationEndpoint {

    private static final String USER_EXISTS = "User with email: %s already exists";
    private static final String CAN_NOT_PERSIST = "Could not save Player to database \nException: %s";
    private static final String APP_HOST_PORT = "http://localhost:8080";

    private final RatingCalculator ratingCalculator;
    private final BCryptPasswordEncoder encoder;
    private final PlayerService playerService;
    private final RatingRepository ratingRepository;

    @PostMapping(path = "registration")
    public Mono<ResponseEntity<String>> registration(@RequestBody Player.Credentials credentials) {
        final String email = credentials.getEmail();
        return playerService.findByEmail(email)
                .flatMap((player) -> Mono.just(ResponseEntity.badRequest().body(String.format(USER_EXISTS, email))))
                .switchIfEmpty(newPlayerFrom(credentials));
    }

    private Mono<ResponseEntity<String>> newPlayerFrom(@RequestBody Player.Credentials credentials) {
        ObjectId id = new ObjectId();
        ratingRepository.insert(initialRating(id));
        return playerService
                .insert(newPlayer(credentials, id))
                .flatMap(player -> ratingRepository.insert(initialRating(player.getId())))
                .map(this::getObjectResponseEntity)
                .doOnError((e) -> new PongManiaException(String.format(CAN_NOT_PERSIST, e.getMessage())));
    }

    private Player newPlayer(Player.Credentials credentials, ObjectId id) {
        return new Player(id, encodedCredentials(credentials), new String[]{"ROLE_USER"});
    }

    private Rating initialRating(ObjectId id) {
        return new Rating(id.toString(), now(), ratingCalculator);
    }

    /**
     * Encodes {@link Player} {@literal password} from {@link Player.Credentials} using {@link BCryptPasswordEncoder}.
     * */
    private Player.Credentials encodedCredentials(Player.Credentials credentials) {
        credentials.setPassword(encoder.encode(credentials.getPassword()));
        return credentials;
    }

    private ResponseEntity<String> getObjectResponseEntity(Rating registered) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/player/")
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
