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

import java.time.LocalDateTime;
import java.util.Map;

import static com.konanov.model.person.Player.Star.FIVE;
import static com.konanov.model.person.Player.Star.FOUR;
import static com.konanov.model.person.Player.Star.ONE;
import static com.konanov.model.person.Player.Star.THREE;
import static com.konanov.model.person.Player.Star.TWO;
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

    public Mono<FinalScore> calculateGame(Mono<Game> calculatedGame) {
        Game game = calculatedGame.block();
        RatingPeriodResults gameResult = new RatingPeriodResults();
        //TODO ANOTHER REACTIVE TRY
        /*Mono<String> hostId = calculatedGame.map(Game::getHostId).map(ObjectId::toString);
        Mono<String> guestId = calculatedGame.map(Game::getGuestId).map(ObjectId::toString);
        Mono<Map<String, Rating>> updatedRatings = hostId.concatWith(guestId).flatMap(ratingService::latestRating)
                .transform(ratingFlux -> ratingFlux
                        .flatMap(rating -> {
                            rating.setRatingDeviation(calculateRatingDeviation(rating, MONTHS.between(rating.getTimestamp(), LocalDateTime.now())));
                            return Mono.just(rating);
                        })).collectMap(Rating::getUid);

        calculatedGame.flatMapIterable(Game::getMatches)
                .map(match -> {
                   updatedRatings.flatMap(pair -> {
                       Mono<Integer> hostScore = hostId.map(id -> match.getMatchResult().get(id)).map(Match.Score::getPoints);
                       Mono<Integer> guestScore = guestId.map(id -> match.getMatchResult().get(id)).map(Match.Score::getPoints);
                       Flux.zip(hostScore, guestScore, (one, two) -> {
                           if (one > two) {
                                gameResult.addResult(pair.get(one), pair.get(two));
                           } else {

                           }
                           return null;
                       });
                   })
                })*/
        final String hostId = game.getHostId().toString();
        final String guestId = game.getGuestId().toString();
        final Rating hostRating = ratingService.latestRating(hostId).block();
        final Rating guestRating = ratingService.latestRating(guestId).block();
        long hostMonthsBetween = MONTHS.between(hostRating.getTimestamp(), LocalDateTime.now());
        long guestMonthsBetween = MONTHS.between(guestRating.getTimestamp(), LocalDateTime.now());
        hostRating.setRatingDeviation(calculateRatingDeviation(hostRating, hostMonthsBetween));
        guestRating.setRatingDeviation(calculateRatingDeviation(guestRating, guestMonthsBetween));
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
        return Flux.zip(ratingService.insert(hostRating), ratingService.insert(guestRating), FinalScore::new).last();
    }

    private double calculateRatingDeviation(Rating player, long hostMonthsBetween) {
        return Math.min(Math.sqrt(Math.pow(player.getRatingDeviation(), 2) + (Math.pow(63.2, 2) * hostMonthsBetween)), 350);
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
