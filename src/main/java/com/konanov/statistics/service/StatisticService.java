package com.konanov.statistics.service;

import com.konanov.statistics.model.Statistic;
import com.konanov.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticsRepository statisticsRepository;

    public Mono<Statistic> insert(Statistic statistic) {
        return statisticsRepository.insert(statistic);
    }
}
