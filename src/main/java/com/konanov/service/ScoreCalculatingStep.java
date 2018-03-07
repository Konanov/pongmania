package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.exceptions.PongManiaException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

import static com.konanov.model.person.Player.Star.*;
import static java.util.stream.Collectors.toMap;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculatingStep implements Resolver<Game, ScoreCalculatingStep.FinalScore> {

    private final PlayerRepository playerRepository;

    private Subscriber<? super FinalScore> scoreSubscriber;

    private static final double LOWERING_COEFFICIENT = 0.001;

    private Map<Integer, Player.Star> starRatings = asSet(ONE, TWO, THREE, FOUR, FIVE)
            .stream().collect(toMap(Player.Star::getNumber, v -> v));

    private double gameScore(Collection<Match> matches, Player player) {
        return matches.stream()
                      .map(Match::getMatchResult)
                      .map(result -> result.get(player.getCredentials().getUserName()))
                      .mapToDouble(score -> score.getPoints() * LOWERING_COEFFICIENT)
                      .sum();
    }

    private Player.Star calculateStars(Double score) {
        return starRatings.get(score.intValue());
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        log.info("Processor received a new subscription");
        //subscription.request(1);
    }

    @Override
    public void onNext(Game game) {
        log.info("Request for calculating game {} scores received", game.getId());
        Collection<Match> matches = game.getMatches();
        final String hostEmail = game.getHostEmail();
        final Player host = getPlayer(hostEmail, "No host player found for email: %s");
        final double hostScore = host.getPoints() + gameScore(matches, host);
        final String guestEmail = game.getGuestEmail();
        final Player guest = getPlayer(guestEmail, "No guest player found for email: %s");
        final double guestScore = guest.getPoints() + gameScore(matches, guest);
        Player.Star hostStar = calculateStars(hostScore);
        Player.Star guestStar = calculateStars(guestScore);
        this.scoreSubscriber.onNext(new FinalScore(game, hostScore, guestScore, hostStar, guestStar));
    }

    private Player getPlayer(String hostEmail, String s) {
        return playerRepository.findByCredentials_Email(hostEmail)
                .orElseThrow(() -> new PongManiaException(String.format(s, hostEmail)));
    }

    @Override
    public void onError(Throwable t) {
        log.error("ERROR");
    }

    @Override
    public void onComplete() {
        log.info("COMPLETED");
    }

    @Override
    public void subscribe(Subscriber<? super FinalScore> scoreSubscriber) {
        this.scoreSubscriber = scoreSubscriber;
    }

    @Getter
    @RequiredArgsConstructor
    static class FinalScore {
        private final Game game;
        private final double hostScore;
        private final double guestScore;
        private final Player.Star hostStar;
        private final Player.Star guestStar;
    }
}
