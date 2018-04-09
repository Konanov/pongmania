package com.konanov.rating.repository;

import com.konanov.rating.model.Rating;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RatingRepository extends ReactiveMongoRepository<Rating, ObjectId> {
    Mono<Rating> findFirstByUidOrderByTimestampDesc(String uid);
}
