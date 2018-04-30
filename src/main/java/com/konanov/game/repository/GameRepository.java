package com.konanov.game.repository;

import com.konanov.game.model.Game;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

public interface GameRepository extends ReactiveMongoRepository<Game, ObjectId> {
    Flux<Game> findByHostId(ObjectId id);

    Flux<Game> findByGuestId(ObjectId id);

    Mono<Long> countByHostId(ObjectId id);

    Mono<Long> countByGuestId(ObjectId id);

    Mono<Long> countByHostIdAndApprovedAndPlanedGameDateLessThan(ObjectId id, ZonedDateTime time);

    Mono<Long> countByGuestIdAndApprovedAndPlanedGameDateLessThan(ObjectId id, ZonedDateTime time);
}
