package com.konanov.config;

import org.goochjs.glicko2.RatingCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {

    @Bean
    public RatingCalculator ratingCalculator() {
        return new RatingCalculator();
    }
}
