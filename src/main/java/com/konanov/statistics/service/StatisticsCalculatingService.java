package com.konanov.statistics.service;

import com.konanov.game.model.Game;
import com.konanov.game.model.Match;
import com.konanov.game.service.GameService;
import com.konanov.statistics.model.Statistic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.DoubleSummaryStatistics;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.summarizingDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCalculatingService {

    private final GameService gameService;
    private final StatisticService statisticService;

    /**
     * Calculates statistics of players participated in game. We use {@link Game} as input only to
     * identify {@literal Host} and {@literal Guest} of the game.
     * */
    public void calculate(Game game) {
        ObjectId hostId = game.getHostId();
        ObjectId guestId = game.getGuestId();
        Flux<Game> hostGames = gameService.findAllPlayerGames(hostId);
        Flux<Game> guestGames = gameService.findAllPlayerGames(guestId);
        Mono<DoubleSummaryStatistics> hostStatistics = matchStatistics(hostId, hostGames);
        Mono<DoubleSummaryStatistics> guestStatistics = matchStatistics(guestId, guestGames);
        Mono<Double> hostMatchWinRatio = matchWinRatio(hostId, hostGames);
        Mono<Double> guestMatchWinRatio = matchWinRatio(guestId, guestGames);
        Mono<Statistic> hostStatistic = collectStatistics(hostId, hostGames, hostStatistics, hostMatchWinRatio);
        Mono<Statistic> guestStatistic = collectStatistics(guestId, guestGames, guestStatistics, guestMatchWinRatio);
        hostStatistic.concatWith(guestStatistic).publishOn(Schedulers.parallel())
                .flatMap(statisticService::insert)
                .map(Statistic::getPlayerUuid)
                .subscribe(id -> log.info("New statistic for player {} is saved", id));
    }

    private Mono<Statistic> collectStatistics(ObjectId guestId, Flux<Game> guestGames,
                                              Mono<DoubleSummaryStatistics> guestStatistics,
                                              Mono<Double> guestMatchWinRatio) {
        return getStatistics(guestId, guestStatistics, guestMatchWinRatio)
                .flatMap(statistic -> guestGames.count().map((count) -> {
                    statistic.setGamesPlayed(count);
                    return statistic;
                }));
    }

    private Mono<Statistic> getStatistics(ObjectId id, Mono<DoubleSummaryStatistics> hostStatistics,
                                          Mono<Double> hostMatchWinRatio) {
        return Flux.zip(hostStatistics, hostMatchWinRatio, (stats, matchWinRation) -> {
            Statistic statistic = new Statistic();
            statistic.setId(new ObjectId());
            statistic.setPlayerUuid(id);
            statistic.setMatchesPlayed(stats.getCount());
            statistic.setTimestamp(now());
            statistic.setMatchWinRatio(matchWinRation);
            statistic.setAverageScore(stats.getAverage());
            statistic.setScoreSum(stats.getSum());
            return statistic;
        }).last();
    }

    private Mono<Double> matchWinRatio(ObjectId playerId, Flux<Game> hostGames) {
        Mono<Long> playerWinsAsHost = winsAsHost(playerId, hostGames);
        Mono<Long> playerWinsAsGuest = winsAsGuest(playerId, hostGames);
        Mono<Long> allMatches = hostGames.flatMapIterable(Game::getMatches).count();
        return Flux.zip(playerWinsAsHost, playerWinsAsGuest, allMatches)
                .map(scope ->
                        (scope.getT1().doubleValue() + scope.getT2().doubleValue()) / scope.getT3().doubleValue()
                ).last();
    }

    private Mono<Long> winsAsGuest(ObjectId playerId, Flux<Game> hostGames) {
        return hostGames.filter(currentGame -> playerId.equals(currentGame.getGuestId()))
                .flatMapIterable(Game::getMatches).map(match -> !match.isHostWon()).filter(win -> win).count();
    }

    private Mono<Long> winsAsHost(ObjectId playerId, Flux<Game> hostGames) {
        return hostGames.filter(currentGame -> playerId.equals(currentGame.getHostId()))
        .flatMapIterable(Game::getMatches).map(Match::isHostWon).filter(win -> win).count();
    }

    private Mono<DoubleSummaryStatistics> matchStatistics(ObjectId playerId, Flux<Game> allGames) {
        return allGames.flatMapIterable(Game::getMatches)
                .map(match -> (double) match.getMatchResult().get(playerId.toString()).getPoints())
                .collect(summarizingDouble(Double::doubleValue));
    }
}
