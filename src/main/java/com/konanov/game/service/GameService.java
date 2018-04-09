package com.konanov.game.service;

import com.konanov.game.repository.GameRepository;
import com.konanov.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Game> save(Game game) {
        return gameRepository.save(game);
    }

    public Flux<Game> findAllUserGames(ObjectId id) {
        return gameRepository.findByHostId(id).concatWith(gameRepository.findByGuestId(id));
    }
}
