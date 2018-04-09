package com.konanov.statistics.repository;

import com.konanov.statistics.model.Statistic;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StatisticsRepository extends ReactiveMongoRepository<Statistic, ObjectId> {
}
