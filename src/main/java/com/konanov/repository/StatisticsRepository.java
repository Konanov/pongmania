package com.konanov.repository;

import com.konanov.model.person.Statistic;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StatisticsRepository extends ReactiveMongoRepository<Statistic, ObjectId> {
}
