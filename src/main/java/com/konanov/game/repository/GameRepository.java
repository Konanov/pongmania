package com.konanov.game.repository;

import com.konanov.game.model.Game;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GameRepository extends ReactiveMongoRepository<Game, ObjectId> {
    Flux<Game> findByHostId(ObjectId id);

    Flux<Game> findByGuestId(ObjectId id);

    Mono<Long> countByHostIdAndApproved(ObjectId id, boolean approved);

    Mono<Long> countByGuestIdAndApproved(ObjectId id, boolean approved);
}
