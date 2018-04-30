package com.konanov.game.service;

import com.konanov.game.model.Game;
import com.konanov.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZonedDateTime.now;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public Mono<Game> findById(ObjectId uuid) {
        return gameRepository.findById(uuid);
    }

    public Mono<Game> insert(Game game) {
        return gameRepository.insert(game);
    }

    public Mono<Map<ObjectId, Long>> countPlayerPlannedGames(ObjectId id) {
        Map<ObjectId, Long> gamesCount = new HashMap<>();
        return gameRepository.countByHostId(id)
                .concatWith(gameRepository.countByGuestId(id))
                .reduce((asHost, asGuest) -> asHost + asGuest).map(count -> {
                    gamesCount.put(id, count);
                    return gamesCount;
                });
    }

    public Mono<Map<ObjectId, Long>> countPlayerPlayedGames(ObjectId id) {
        ZonedDateTime now = now();
        Map<ObjectId, Long> gamesCount = new HashMap<>();
        return gameRepository.countByHostIdAndApprovedAndPlanedGameDateLessThan(id, now)
                .concatWith(gameRepository.countByGuestIdAndApprovedAndPlanedGameDateLessThan(id, now))
                .reduce((asHost, asGuest) -> asHost + asGuest).map(count -> {
                    gamesCount.put(id, count);
                    return gamesCount;
                });
    }

    public Mono<Game> save(Game game) {
        return gameRepository.save(game);
    }

    public Flux<Game> findAllUserGames(ObjectId id) {
        return gameRepository.findByHostId(id).concatWith(gameRepository.findByGuestId(id));
    }
}
