package com.konanov.repository;

import com.konanov.model.person.Player;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player, ObjectId> {
    Optional<Player> findByCredentials_UserName(String userName);
    Optional<Player> findByCredentials_Email(String email);
}
