package com.konanov.player.repository;

import com.konanov.league.model.PublicLeagueType;
import com.konanov.player.model.Player;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlayerRepository extends ReactiveMongoRepository<Player, ObjectId> {
    Mono<Player> findByCredentials_Email(String email);
    Mono<Player> findById(ObjectId id);
    Flux<Player> findByPublicLeague_TypeOrderByLatestRating_RatingDesc(PublicLeagueType type);
    Flux<Player> findByPublicLeague_Type(PublicLeagueType type);
    Mono<Long> countByPublicLeague_Type(PublicLeagueType type);
}
