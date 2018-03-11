package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private Subscriber<? super FinalScore> scoreSubscriber;

    private static final double LOWERING_COEFFICIENT = 0.001;

    private Map<Integer, Player.Star> starRatings = asSet(ONE, TWO, THREE, FOUR, FIVE)
            .stream().collect(toMap(Player.Star::getNumber, v -> v));

    private Mono<Integer> gameScore(Flux<Match> matches, Mono<Player> playerMono) {
        //items.map(CalculableItem::getHost).map(playerMono -> playerMono.map(Player::getCredentials).map(Player.Credentials::getUserName));
        //items.map(item -> {
        //    Flux.zip(item, item.getHost())
        //    item.getMatches().map(match -> match.getMatchResult().get(item.getGuest().map(Player::getCredentials).map(Player.Credentials::getUserName)));
        //    item.getHost();
        //    item.getGuest();
        //})
        return Flux.zip(matches, playerMono.cache().repeat(),
                (match, player) -> match.getMatchResult().get(player.getUsername()))
                .map(Match.Score::getPoints)
                .reduce((match1, match2) -> match1 + match2);
    }

    private Player.Star calculateStars(Double score) {
        return starRatings.get(score.intValue());
    }

    public Mono<FinalScore> caculateGame(Mono<Game> game) {
        Flux<Match> matchFlux = game.map(Game::getMatches).flatMapMany(Flux::fromIterable);
        Mono<Integer> hostScore = getPlayerScore(matchFlux, game.map(Game::getHostEmail));
        Mono<Integer> guestScore = getPlayerScore(matchFlux, game.map(Game::getGuestEmail));
        return Flux.zip(hostScore, guestScore, FinalScore::new).next();
        //game.log()
        //        .map(this::convertToCalculableItem)
        //        .map(this::gameScore)
        //log.info("Request for calculating game {} scores received", game.getId());
        //Collection<Match> matches = game.getMatches();
        //final String hostEmail = game.getHostEmail();
        //final Player host = getPlayer(hostEmail, "No host player found for email: %s");
        //final double hostScore = host.getPoints() + gameScore(matches, host);
        //final String guestEmail = game.getGuestEmail();
        //final Player guest = getPlayer(guestEmail, "No guest player found for email: %s");
        //final double guestScore = guest.getPoints() + gameScore(matches, guest);
        //Player.Star hostStar = calculateStars(hostScore);
        //Player.Star guestStar = calculateStars(guestScore);
        //this.scoreSubscriber.onNext(new FinalScore(game, hostScore, guestScore, hostStar, guestStar));
    }

    private Mono<Integer> getPlayerScore(Flux<Match> matches, Mono<String> map) {
        return Flux.zip(matches, map.cache().repeat(),
                (match, playerEmail) -> match.getMatchResult().get(playerEmail).getPoints())
                .reduce((prevMatch, nextMatch) -> prevMatch + nextMatch);
    }

    private Flux<CalculableItem> convertToCalculableItem(Game item) {
        Mono<Player> host = getPlayer(item.getHostEmail());
        Mono<Player> guest = getPlayer(item.getGuestEmail());
        Flux<Match> matches = Flux.fromIterable(item.getMatches());
        return Flux.just(new CalculableItem(host, guest, matches));
    }

    @Getter
    @RequiredArgsConstructor
    private class CalculableItem {
        private final Mono<Player> host;
        private final Mono<Player> guest;
        private final Flux<Match> matches;
    }

    private Mono<Player> getPlayer(String hostEmail) {
        return playerRepository.findByCredentials_Email(hostEmail);
    }

    @Getter
    @RequiredArgsConstructor
    public static class FinalScore {
        //private final Game game;
        //private final double hostScore;
        //private final double guestScore;
        //private final Player.Star hostStar;
        //private final Player.Star guestStar;
        private final int hostScore;
        private final int guestScore;
    }
}
