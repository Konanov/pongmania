package com.konanov.repository;

import com.konanov.model.person.Player;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PlayerRepository extends ReactiveMongoRepository<Player, ObjectId> {
    Optional<Player> findByCredentials_UserName(String userName);
    Mono<Player> findByCredentials_Email(String email);
}
