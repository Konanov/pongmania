package com.konanov.service;

import com.konanov.gliko.Rating;
import com.konanov.gliko.RatingCalculator;
import com.konanov.gliko.RatingPeriodResults;
import com.konanov.model.game.Game;
import com.konanov.service.model.RatingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MONTHS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculatingStep {

    private final RatingService ratingService;
    private final RatingCalculator ratingCalculator;

    /**
     * Calculates RatingDeviation before resolving match to calibrate players who did not play for a while.
     * Then calculates Rating for every player resolving game outcome.
     * */
    public Flux<Rating> calculate(Game game) {
        RatingPeriodResults gameResult = new RatingPeriodResults();
        final String hostId = game.getHostId().toString();
        final String guestId = game.getGuestId().toString();
        final Rating hostRating = ratingService.latestRating(hostId).block();
        final Rating guestRating = ratingService.latestRating(guestId).block();
        long hostMonthsAfterLastGame = monthsAfterLastGame(hostRating);
        long guestMonthsAfterLastGame = monthsAfterLastGame(guestRating);
        hostRating.setRatingDeviation(calculateRatingDeviation(hostRating, hostMonthsAfterLastGame));
        guestRating.setRatingDeviation(calculateRatingDeviation(guestRating, guestMonthsAfterLastGame));
        game.getMatches().forEach(match -> {
            int hostScore = match.getMatchResult().get(hostId).getPoints();
            int guestScore = match.getMatchResult().get(guestId).getPoints();
            if (hostScore > guestScore) {
                gameResult.addResult(hostRating, guestRating);
            } else {
                gameResult.addResult(guestRating, hostRating);
            }
        });
        ratingCalculator.updateRatings(gameResult);
        hostRating.setId(new ObjectId());
        hostRating.setTimestamp(now());
        guestRating.setId(new ObjectId());
        guestRating.setTimestamp(now());
        return ratingService.insert(hostRating).concatWith(ratingService.insert(guestRating));
    }

    private long monthsAfterLastGame(Rating hostRating) {
        return MONTHS.between(hostRating.getTimestamp(), now());
    }

    private double calculateRatingDeviation(Rating player, long hostMonthsBetween) {
        return Math.min(Math.sqrt(Math.pow(player.getRatingDeviation(), 2)
                + (Math.pow(63.2, 2) * hostMonthsBetween)), 350);
    }

    @Getter
    @RequiredArgsConstructor
    static class FinalScore {
        private final Rating hostRating;
        private final Rating guestRating;
    }
}
