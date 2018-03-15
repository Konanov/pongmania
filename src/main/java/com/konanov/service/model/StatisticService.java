package com.konanov.service.model;

import com.konanov.model.person.Statistic;
import com.konanov.repository.StatisticsRepository;
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
