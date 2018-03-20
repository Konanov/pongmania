package com.konanov.repository;

import com.konanov.model.person.Player;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PlayerRepository extends ReactiveMongoRepository<Player, ObjectId> {
    Mono<Player> findByCredentials_Email(String email);
    Mono<Player> findById(ObjectId id);
}
