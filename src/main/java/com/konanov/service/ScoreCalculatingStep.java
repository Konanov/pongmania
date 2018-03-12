package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.goochjs.glicko2.Rating;
import org.goochjs.glicko2.RatingCalculator;
import org.goochjs.glicko2.RatingPeriodResults;
import org.goochjs.glicko2.Result;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.konanov.model.person.Player.Star.*;
import static java.util.stream.Collectors.toMap;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculatingStep {

    private final PlayerRepository playerRepository;
    private final RatingCalculator ratingCalculator;

    private Subscriber<? super FinalScore> scoreSubscriber;

    private static final double LOWERING_COEFFICIENT = 0.001;

    private Map<Integer, Player.Star> starRatings = asSet(ONE, TWO, THREE, FOUR, FIVE)
            .stream().collect(toMap(Player.Star::getNumber, v -> v));

    private Player.Star calculateStars(Double score) {
        return starRatings.get(score.intValue());
    }

    public void calculateGame(Game game) {
        Player host = playerRepository.findById(game.getHostId()).block();
        Player guest = playerRepository.findById(game.getGuestId()).block();
        RatingPeriodResults gameResult = new RatingPeriodResults();
        final Rating hostRating = host.getRating();
        final Rating guestRating = guest.getRating();
        System.out.println("<<<<<<<<<< BEFORE CALCULATION >>>>>>>>>>>>>>>>>");
        System.out.println("HOST RATING = " + hostRating.getRating());
        System.out.println("GUEST RATING = " + guestRating.getRating());
        System.out.println("HOST RATING DEVIATION = " + hostRating.getRatingDeviation());
        System.out.println("GUEST RATING DEVIATION = " + guestRating.getRatingDeviation());
        System.out.println("HOST VOLATILITY= " + hostRating.getVolatility());
        System.out.println("GUEST VOLATILITY = " + guestRating.getVolatility());
        game.getMatches().forEach(match -> {
            int hostScore = match.getMatchResult().get(host.getId().toString()).getPoints();
            int guestScore = match.getMatchResult().get(guest.getId().toString()).getPoints();
            if (hostScore > guestScore) {
                gameResult.addResult(hostRating, guestRating);
            } else {
                gameResult.addResult(guestRating, hostRating);
            }
        });
        ratingCalculator.updateRatings(gameResult);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("<<<<<<<<<< AFTER CALCULATION >>>>>>>>>>>>>>>>>");
        System.out.println("HOST RATING = " + hostRating.getRating());
        System.out.println("GUEST RATING = " + guestRating.getRating());
        System.out.println("HOST RATING DEVIATION = " + hostRating.getRatingDeviation());
        System.out.println("GUEST RATING DEVIATION = " + guestRating.getRatingDeviation());
        System.out.println("HOST VOLATILITY= " + hostRating.getVolatility());
        System.out.println("GUEST VOLATILITY = " + guestRating.getVolatility());

        //TODO AWESOME REACTIVE CODE TO REFACTOR
        /*Mono<RatingPeriodResults> gameResult = Mono.just(new RatingPeriodResults());
        Mono<Player> hostPlayer = playerRepository.findById(game.map(Game::getHostId));
        Mono<Player> guestPlayer = playerRepository.findById(game.map(Game::getGuestId));
        Flux<Match> matchFlux = game.map(Game::getMatches).flatMapMany(Flux::fromIterable);
        Flux<Result> results = Flux.zip(matchFlux, hostPlayer, guestPlayer, gameResult)
                .flatMap(match -> {
                    int hostScore = match.getT1().getMatchResult().get(match.getT2().getId().toString()).getPoints();
                    int guestScore = match.getT1().getMatchResult().get(match.getT3().getId().toString()).getPoints();
                    if (hostScore > guestScore) {
                        match.getT4().addResult(match.getT2().getRating(), match.getT3().getRating());
                        return Mono.just(new Result(match.getT2().getRating(), match.getT3().getRating()));
                    } else {
                        match.getT4().addResult(match.getT3().getRating(), match.getT2().getRating());
                        ratingCalculator.updateRatings(match.getT4());
                        return Mono.just(new Result(match.getT3().getRating(), match.getT2().getRating()));
                    }
                });
        */


        //results.flatMapIterable(result -> gameResult.flatMapMany(kek -> kek.addResult(result.getWinner(), result.getLoser())))
        //ratingCalculator.updateRatings();
        //matchResults.flatMapIterable(result -> gameResult.flatMap(currentGame -> currentGame.addResult(result)))
        //Mono<Integer> hostScore = getPlayerScore(matchFlux, game.map(Game::getHostId));
        //Mono<Integer> guestScore = getPlayerScore(matchFlux, game.map(Game::getGuestId));
        //return Flux.zip(hostScore, guestScore, FinalScore::new).single();
    }

    private Mono<Integer> getPlayerScore(Flux<Match> matches, Mono<ObjectId> id) {
        return Flux.zip(matches, id.cache().repeat(),
                (match, playerUuid) -> match.getMatchResult().get(playerUuid.toString()).getPoints())
                .reduce((prevMatch, nextMatch) -> prevMatch + nextMatch);
    }

    @Getter
    @RequiredArgsConstructor
    public static class FinalScore {
        private final int hostScore;
        private final int guestScore;
    }
}
