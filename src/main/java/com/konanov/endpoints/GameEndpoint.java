package com.konanov.endpoints;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.repository.GameRepository;
import com.konanov.service.ScoreCalculatingStep;
import com.konanov.service.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.util.ObjectUtils.isEmpty;

@RestController
@RequiredArgsConstructor
public class GameEndpoint implements Publisher<Game> {

    private final GameRepository gameRepository;
    private final ScoreCalculatingStep scoreCalculatingStep;
    private Subscriber<? super Game> subscriber;

    @PostConstruct
    private void subscribeCalculator() {
        this.subscribe(scoreCalculatingStep);
    }

    @Override
    public void subscribe(Subscriber<? super Game> subscriber) {
        this.subscriber = subscriber;
    }

    @PostMapping("/game/{uuid}/calculate")
    public void postGame(@PathVariable String uuid) {
        Game game = gameRepository.findById(new ObjectId(uuid))
                .orElseThrow(() -> new PongManiaException(String.format("No game found for uuid: %s", uuid)));
        subscriber.onNext(game);
    }

    @PostMapping("/game/offer")
    public ResponseEntity<Object> offerGame(@RequestBody Game game) {
        Game savedGame = gameRepository.insert(game);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedGame.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/game/{uuid}")
    public Game findOne(@PathVariable String uuid) {
        return gameRepository.findById(new ObjectId(uuid))
                .orElseThrow(() -> new PongManiaException(String.format("No game found for uuid: %s", uuid)));
    }

    @GetMapping("/game/all")
    public Collection<Game> allGames() {
        return gameRepository.findAll();
    }

    @PostMapping("/game/{gameUuid}/add/match")
    public Game addMatch(@PathVariable String gameUuid, @RequestBody Match match) {
        Game gameToUpdate = gameRepository.findById(new ObjectId(gameUuid))
                .orElseThrow(() -> new PongManiaException(String.format("No game found for uuid: %s", gameUuid)));
        Collection<Match> matches = gameToUpdate.getMatches();
        if (isEmpty(matches)) {
            matches = new ArrayList<>();
        }
        matches.add(match);
        gameToUpdate.setMatches(matches);
        return gameRepository.save(gameToUpdate);
    }
}
