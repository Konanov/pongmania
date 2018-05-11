package com.konanov.league.repository;

import com.konanov.league.model.PrivateLeague;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Created by alex on 5/12/18.
 */
public interface PrivateLeagueRepository extends ReactiveMongoRepository<PrivateLeague, ObjectId> {
    public Mono<PrivateLeague> findByName(String name);
}
