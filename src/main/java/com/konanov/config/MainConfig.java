package com.konanov.config;

import com.konanov.rating.service.RatingCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {

    @Bean
    public RatingCalculator ratingCalculator() {
        return new RatingCalculator();
    }
}
