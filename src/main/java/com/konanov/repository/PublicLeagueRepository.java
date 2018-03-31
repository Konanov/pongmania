package com.konanov.repository;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PublicLeagueRepository extends ReactiveMongoRepository<PublicLeague, ObjectId> {
    Mono<PublicLeague> findByType(String type);
}