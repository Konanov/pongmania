package com.konanov.league.model;

import reactor.core.publisher.Mono;

public enum PublicLeagueType {
    JUNIOR, MIDDLE, PRO;

    public static Mono<PublicLeagueType> leagueByName(String name) {
        switch (name) {
            case "Junior":
                return Mono.just(JUNIOR);
            case "Middle":
                return Mono.just(MIDDLE);
            case "Pro":
                return Mono.just(PRO);
            default:
                return Mono.empty();
        }
    }
}
