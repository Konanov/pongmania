package com.konanov.endpoints;

import com.konanov.gliko.Rating;
import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Statistic;
import com.konanov.service.ScoreCalculatingStep;
import com.konanov.service.StatisticsCalculatingStep;
import com.konanov.service.model.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GameEndpoint {

    private final GameService gameService;
    private final ScoreCalculatingStep scoreCalculatingStep;
    private final StatisticsCalculatingStep statisticsCalculatingStep;

    private static final String APP_HOST_PORT = "http://localhost:8080";

    /**
     * Offer new {@link Game} to player.
     * Game created without any {@link Match} present.
     * {@link Game} timestamp should be in future, as game can only be planned. You can not offer {@link Game}
     * that was held in past.
     * */
    @PostMapping("/game/offer")
    public Mono<ResponseEntity<String>> offerGame(@RequestBody Game game) {
        return gameService.insert(game).flatMap(this::getObjectResponseEntity);
    }

    /**
     * Add {@link Match} to existing game. {@link Match} should contain scores for
     * {@literal Host} and {@literal Guest} players of the game.
     * */
    @PostMapping("/game/{uuid}/addMatch")
    public Mono<Game> addMatch(@PathVariable String uuid, @RequestBody Match match) {
        Mono<Game> game = gameService.findById(new ObjectId(uuid));
        game.map(Game::getMatches).doOnNext(matches -> matches.add(match));
        return game.flatMap(gameService::save);
    }

    /**
     * Calculate {@link Game} results and save new {@link Rating} and {@link Statistic} for both players.
     * */
    @PostMapping("/game/{uuid}/calculate")
    public Flux<Rating> calculateGame(@PathVariable String uuid) {
        final Mono<Game> game = gameService.findById(new ObjectId(uuid));
        game.map(statisticsCalculatingStep::calculate)
                .subscribe(stats -> stats.log("New statistics saved for user"));
        return game.flatMapMany(scoreCalculatingStep::calculateGame);
    }

    @GetMapping("/game/{uuid}/all")
    public Flux<Game> getUserGames(@PathVariable String uuid) {
        return gameService.findAllUserGames(new ObjectId(uuid));
    }

    private Mono<ResponseEntity<String>> getObjectResponseEntity(Game registered) {
        URI location = ServletUriComponentsBuilder.fromUriString(APP_HOST_PORT + "/game/")
                .path("/{id}")
                .buildAndExpand(registered.getId())
                .toUri();
        return Mono.just(ResponseEntity.created(location).build());
    }
}
