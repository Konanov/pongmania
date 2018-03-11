package com.konanov.endpoints;

import com.konanov.model.game.Game;
import com.konanov.service.ScoreCalculatingStep;
import com.konanov.service.model.GameService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class GameEndpoint {

    private final GameService gameService;
    private final ScoreCalculatingStep scoreCalculatingStep;

    private static final String APP_HOST_PORT = "http://localhost:8080";

    @PostMapping("/game/{uuid}/calculate")
    public Mono<ScoreCalculatingStep.FinalScore> calculateGame(@PathVariable Mono<String> uuid) {
        return uuid.map(ObjectId::new)
                .map(gameService::findById)
                .flatMap(scoreCalculatingStep::caculateGame);
    }

    @PostMapping("/game/offer")
    public Mono<ResponseEntity<String>> offerGame(@RequestBody Mono<Game> game) {
        return game.flatMap(gameService::insert).flatMap(this::getObjectResponseEntity);
    }

    private Mono<ResponseEntity<String>> getObjectResponseEntity(Game registered) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/game/")
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return Mono.just(ResponseEntity.created(location).build());
    }

//    @GetMapping("/game/{uuid}")
//    public Game findOne(@PathVariable String uuid) {
//        return gameRepository.findById(new ObjectId(uuid))
//                .orElseThrow(() -> new PongManiaException(String.format("No game found for uuid: %s", uuid)));
//    }
//
//    @GetMapping("/game/all")
//    public Collection<Game> allGames() {
//        return gameRepository.findAll();
//    }
//
//    @PostMapping("/game/{gameUuid}/add/match")
//    public Game addMatch(@PathVariable String gameUuid, @RequestBody Match match) {
//        Game gameToUpdate = gameRepository.findById(new ObjectId(gameUuid))
//                .orElseThrow(() -> new PongManiaException(String.format("No game found for uuid: %s", gameUuid)));
//        Collection<Match> matches = gameToUpdate.getMatches();
//        if (isEmpty(matches)) {
//            matches = new ArrayList<>();
//        }
//        matches.add(match);
//        gameToUpdate.setMatches(matches);
//        return gameRepository.save(gameToUpdate);
//    }
}
