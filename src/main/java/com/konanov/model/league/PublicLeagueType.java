package com.konanov.model.league;

import java.util.Optional;

public enum PublicLeagueType {
    JUNIOR, MIDDLE, PRO;

    public static Optional<PublicLeagueType> leagueByName(String name) {
        switch (name) {
            case "Junior":
                return Optional.of(JUNIOR);
            case "Middle":
                return Optional.of(MIDDLE);
            case "Pro":
                return Optional.of(PRO);
            default:
                return Optional.empty();
        }
    }
}
