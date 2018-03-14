package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.model.person.Statistic;
import com.konanov.service.model.GameService;
import com.konanov.service.model.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.IntSummaryStatistics;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summarizingInt;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCalculatingStep {

    private final PlayerService playerService;
    private final GameService gameService;

    public Flux<Statistic> calculate(Game game) {
        Mono<Game> newGame = gameService.save(game);
        ObjectId hostId = game.getHostId();
        ObjectId guestId = game.getGuestId();
        Mono<Player> host = playerService.findById(hostId);
        Mono<Player> guest = playerService.findById(guestId);
        Flux<Game> hostGames = gameService.findAllUserGames(hostId);
        Flux<Game> guestGames = gameService.findAllUserGames(guestId);
        Mono<IntSummaryStatistics> hostStatistics = matchStatistics(hostId, newGame, hostGames);
        Mono<IntSummaryStatistics> guestStatistics = matchStatistics(guestId, newGame, guestGames);
        Mono<Long> hostWinsAsHost = winsAsHost(newGame, hostId, hostGames);
        Mono<Long> hostWinsAsGuest = winsAsGuest(newGame, hostId, hostGames);
        Mono<Long> guestWinsAsHost = winsAsGuest(newGame, guestId, guestGames);
        Mono<Long> guestWinsAsGuest = winsAsGuest(newGame, guestId, guestGames);
        Mono<Long> allHostGames = newGame.concatWith(hostGames).flatMapIterable(Game::getMatches).count();
        Mono<Double> hostMatchWinRatio = Flux.zip(hostWinsAsHost, hostWinsAsGuest, allHostGames)
                .map(scope ->(scope.getT1().doubleValue() + scope.getT2().doubleValue()) / scope.getT3().doubleValue()).last();
        Mono<Double> guestMatchWinRatio = Flux.zip(guestWinsAsHost, guestWinsAsGuest, allHostGames)
                .map(scope ->(scope.getT1().doubleValue() + scope.getT2().doubleValue()) / scope.getT3().doubleValue()).last();
        Mono<Statistic> hostStatistic = Flux.zip(hostStatistics, hostMatchWinRatio, (stats, matchWinRation) -> {
            Statistic statistic = new Statistic();
            statistic.setMatchWinRatio(matchWinRation);
            statistic.setAvarageScore(stats.getAverage());
            statistic.setScoreSum(stats.getSum());
            return statistic;
        }).last();
        return null;
    }

    private Mono<Long> winsAsGuest(Mono<Game> newGame, ObjectId playerId, Flux<Game> hostGames) {
        return newGame.concatWith(hostGames).filter(currentGame -> playerId == currentGame.getGuestId())
                .flatMapIterable(Game::getMatches).map(match -> !match.isHostWon()).collect(counting());
    }

    private Mono<Long> winsAsHost(Mono<Game> newGame, ObjectId playerId, Flux<Game> hostGames) {
        return newGame.concatWith(hostGames).filter(currentGame -> playerId == currentGame.getHostId())
        .flatMapIterable(Game::getMatches).map(Match::isHostWon).collect(counting());
    }

    private Mono<IntSummaryStatistics> matchStatistics(ObjectId playerId, Mono<Game> newGame, Flux<Game> allGames) {
        return newGame.concatWith(allGames).flatMapIterable(Game::getMatches)
                .map(match -> match.getMatchResult().get(playerId.toString()).getPoints())
                .collect(summarizingInt(Integer::intValue));
    }
}
