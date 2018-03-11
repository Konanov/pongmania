package com.konanov.endpoints;

import com.konanov.repository.GameRepository;
import com.konanov.service.ScoreCalculatingStep;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GameEndpoint {

    private final GameRepository gameRepository;
    private final ScoreCalculatingStep scoreCalculatingStep;

    @PostMapping("/game/{uuid}/calculate")
    public Mono<ScoreCalculatingStep.FinalScore> postGame(@PathVariable Mono<String> uuid) {
        return uuid.map(ObjectId::new)
                .map(gameRepository::findById)
                .flatMap(scoreCalculatingStep::caculateGame);
    }

//    @PostMapping("/game/offer")
//    public ResponseEntity<Object> offerGame(@RequestBody Game game) {
//        Game savedGame = gameRepository.insert(game);
//        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(savedGame.getId())
//                .toUri();
//        return ResponseEntity.created(location).build();
//    }
//
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
