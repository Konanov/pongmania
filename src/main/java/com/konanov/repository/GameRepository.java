package com.konanov.repository;

import com.konanov.model.game.Game;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, ObjectId> {
}
