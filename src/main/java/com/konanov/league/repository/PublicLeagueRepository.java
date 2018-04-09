package com.konanov.league.repository;

import com.konanov.league.model.PublicLeague;
import com.konanov.league.model.PublicLeagueType;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PublicLeagueRepository extends ReactiveMongoRepository<PublicLeague, ObjectId> {
    Mono<PublicLeague> findByType(PublicLeagueType type);
}