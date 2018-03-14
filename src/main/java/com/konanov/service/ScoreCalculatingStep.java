package com.konanov.service;

import com.konanov.gliko.Rating;
import com.konanov.gliko.RatingCalculator;
import com.konanov.gliko.RatingPeriodResults;
import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.model.RatingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.konanov.model.person.Player.Star.FIVE;
import static com.konanov.model.person.Player.Star.FOUR;
import static com.konanov.model.person.Player.Star.ONE;
import static com.konanov.model.person.Player.Star.THREE;
import static com.konanov.model.person.Player.Star.TWO;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.toMap;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculatingStep {

    private final PlayerRepository playerRepository;
    private final RatingService ratingService;
    private final RatingCalculator ratingCalculator;

    private Subscriber<? super FinalScore> scoreSubscriber;

    private static final double LOWERING_COEFFICIENT = 0.001;

    private Map<Integer, Player.Star> starRatings = asSet(ONE, TWO, THREE, FOUR, FIVE)
            .stream().collect(toMap(Player.Star::getNumber, v -> v));

    private Player.Star calculateStars(Double score) {
        return starRatings.get(score.intValue());
    }

    /**
     * Calculates RatingDeviation before resolving match to calibrate players who did not play for a while.
     * Then calculates Rating for every player resolving game outcome.
     * */
    public Flux<Rating> calculateGame(Game game) {
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

    private Mono<Integer> getPlayerScore(Flux<Match> matches, Mono<ObjectId> id) {
        return Flux.zip(matches, id.cache().repeat(),
                (match, playerUuid) -> match.getMatchResult().get(playerUuid.toString()).getPoints())
                .reduce((prevMatch, nextMatch) -> prevMatch + nextMatch);
    }

    @Getter
    @RequiredArgsConstructor
    public static class FinalScore {
        private final Rating hostRating;
        private final Rating guestRating;
    }
}
