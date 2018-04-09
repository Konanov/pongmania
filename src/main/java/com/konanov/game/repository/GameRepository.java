package com.konanov.game.repository;

import com.konanov.game.model.Game;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface GameRepository extends ReactiveMongoRepository<Game, ObjectId> {
    Flux<Game> findByHostId(ObjectId id);
    Flux<Game> findByGuestId(ObjectId id);
}
