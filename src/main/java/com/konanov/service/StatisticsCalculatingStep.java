package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.model.person.Statistic;
import com.konanov.service.model.GameService;
import com.konanov.service.model.PlayerService;
import com.konanov.service.model.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.IntSummaryStatistics;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summarizingInt;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCalculatingStep {

    private final GameService gameService;
    private final StatisticService statisticService;

    /**
     * Calculates statistics of players participated in game. We use {@link Game} as input only to
     * identify {@literal Host} and {@literal Guest} of the game.
     * */
    public Flux<Statistic> calculate(Game game) {
        Mono<Game> newGame = gameService.save(game);
        ObjectId hostId = game.getHostId();
        ObjectId guestId = game.getGuestId();
        Flux<Game> hostGames = gameService.findAllUserGames(hostId);
        Flux<Game> guestGames = gameService.findAllUserGames(guestId);
        Mono<IntSummaryStatistics> hostStatistics = matchStatistics(hostId, newGame, hostGames);
        Mono<IntSummaryStatistics> guestStatistics = matchStatistics(guestId, newGame, guestGames);
        Mono<Long> hostWinsAsHost = winsAsHost(hostId, hostGames);
        Mono<Long> hostWinsAsGuest = winsAsGuest(hostId, hostGames);
        Mono<Long> guestWinsAsHost = winsAsHost(guestId, guestGames);
        Mono<Long> guestWinsAsGuest = winsAsGuest(guestId, guestGames);
        Mono<Long> allHostGames = hostGames.flatMapIterable(Game::getMatches).count();
        Mono<Long> allGuestGames = guestGames.flatMapIterable(Game::getMatches).count();
        Mono<Double> hostMatchWinRatio = matchWinRatio(hostWinsAsHost, hostWinsAsGuest, allHostGames);
        Mono<Double> guestMatchWinRatio = matchWinRatio(guestWinsAsHost, guestWinsAsGuest, allGuestGames);
        Mono<Statistic> hostStatistic = getStatistics(hostId, hostStatistics, hostMatchWinRatio);
        Mono<Statistic> guestStatistic = getStatistics(guestId, guestStatistics, guestMatchWinRatio);
        return hostStatistic.concatWith(guestStatistic).flatMap(statisticService::insert);
    }

    private Mono<Statistic> getStatistics(ObjectId id, Mono<IntSummaryStatistics> hostStatistics,
                                          Mono<Double> hostMatchWinRatio) {
        return Flux.zip(hostStatistics, hostMatchWinRatio, (stats, matchWinRation) -> {
            Statistic statistic = new Statistic();
            statistic.setId(new ObjectId());
            statistic.setPlayerUuid(id);
            statistic.setTimestamp(now());
            statistic.setMatchWinRatio(matchWinRation);
            statistic.setAvarageScore(stats.getAverage());
            statistic.setScoreSum(stats.getSum());
            return statistic;
        }).last();
    }

    private Mono<Double> matchWinRatio(Mono<Long> playerWinsAsHost, Mono<Long> playerWinsAsGuest, Mono<Long> allGames) {
        return Flux.zip(playerWinsAsHost, playerWinsAsGuest, allGames)
                .map(scope ->(scope.getT1().doubleValue() + scope.getT2().doubleValue()) / scope.getT3().doubleValue()).last();
    }

    private Mono<Long> winsAsGuest(ObjectId playerId, Flux<Game> hostGames) {
        return hostGames.filter(currentGame -> playerId == currentGame.getGuestId())
                .flatMapIterable(Game::getMatches).map(match -> !match.isHostWon()).collect(counting());
    }

    private Mono<Long> winsAsHost(ObjectId playerId, Flux<Game> hostGames) {
        return hostGames.filter(currentGame -> playerId == currentGame.getHostId())
        .flatMapIterable(Game::getMatches).map(Match::isHostWon).collect(counting());
    }

    private Mono<IntSummaryStatistics> matchStatistics(ObjectId playerId, Mono<Game> newGame, Flux<Game> allGames) {
        return newGame.concatWith(allGames).flatMapIterable(Game::getMatches)
                .map(match -> match.getMatchResult().get(playerId.toString()).getPoints())
                .collect(summarizingInt(Integer::intValue));
    }
}
