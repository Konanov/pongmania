package com.konanov.service.model;

import com.konanov.model.game.Game;
import com.konanov.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
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
}
