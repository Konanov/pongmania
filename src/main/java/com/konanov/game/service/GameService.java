package com.konanov.game.service;

import com.konanov.game.model.Game;
import com.konanov.game.model.Match;
import com.konanov.game.repository.GameRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameService {

  private final GameRepository gameRepository;

  /**
   * Counts not approved games player has planned.
   *
   * @return number games planned by player and maps it to his {@link ObjectId}
   */
  public Mono<Map<ObjectId, Long>> countPlanedGames(ObjectId id) {
    Map<ObjectId, Long> gamesCount = new HashMap<>();
    return gameRepository
        .countByHostIdAndApproved(id, false)
        .concatWith(gameRepository
            .countByGuestIdAndApproved(id, false))
        .reduce((asHost, asGuest) -> asHost + asGuest).map(count -> {
          gamesCount.put(id, count);
          return gamesCount;
        });
  }

  /**
   * Counts approved games player has played.
   *
   * @return number games played by player and maps it to his {@link ObjectId}
   */
  public Mono<Map<ObjectId, Long>> countPlayedGames(ObjectId id) {
    Map<ObjectId, Long> gamesCount = new HashMap<>();
    return gameRepository.countByHostIdAndApproved(id, true)
        .concatWith(
            gameRepository.countByGuestIdAndApproved(id, true))
        .reduce((asHost, asGuest) -> asHost + asGuest).map(count -> {
          gamesCount.put(id, count);
          return gamesCount;
        }).switchIfEmpty(Mono.just(gamesCount));
  }

  public Flux<Map<ObjectId, BigDecimal>> matchWinRatio(ObjectId id) {
    final AtomicInteger matchCount = new AtomicInteger();
    final AtomicInteger wonMatches = new AtomicInteger();
    Map<ObjectId, BigDecimal> matchWinRation = new HashMap<>();
    return gameRepository.findByHostId(id).concatWith(gameRepository.findByGuestId(id))
        .flatMap(game -> {
          final Collection<Match> matches = game.getMatches();
          if (matches != null && !matches.isEmpty()) {
            countWinsAsHost(id, matchCount, wonMatches, game, matches);
            countWinsAsGuest(id, matchCount, wonMatches, game, matches);
          }
          BigDecimal winRatio = getWinRatio(matchCount, wonMatches);
          matchWinRation.put(id, winRatio);
          return Mono.just(matchWinRation);
        }).switchIfEmpty(noMatchResult(matchWinRation, id));
  }

  private Flux<Map<ObjectId, BigDecimal>> noMatchResult(Map<ObjectId, BigDecimal> map,
      ObjectId playerId) {
    map.put(playerId, new BigDecimal(0));
    return Flux.just(map);
  }

  private BigDecimal getWinRatio(AtomicInteger matchCount, AtomicInteger wonMatches) {
    if (wonMatches.intValue() != 0) {
      return BigDecimal.valueOf(wonMatches.get())
          .divide(BigDecimal.valueOf(matchCount.get()), 2, BigDecimal.ROUND_HALF_DOWN)
          .multiply(new BigDecimal(100));
    }
    return new BigDecimal(0);
  }

  private void countWinsAsGuest(ObjectId id, AtomicInteger matchCount, AtomicInteger wonMatches,
      Game game, Collection<Match> matches) {
    if (game.getGuestId().equals(id)) {
      matches.forEach(match -> {
        matchCount.incrementAndGet();
        if (!match.isHostWon()) {
          wonMatches.incrementAndGet();
        }
      });
    }
  }

  private void countWinsAsHost(ObjectId id, AtomicInteger matchCount, AtomicInteger wonMatches,
      Game game, Collection<Match> matches) {
    if (game.getHostId().equals(id)) {
      matches.forEach(match -> {
        matchCount.incrementAndGet();
        if (match.isHostWon()) {
          wonMatches.incrementAndGet();
        }
      });
    }
  }

  public Flux<Game> findAllPlayerGames(ObjectId id) {
    return gameRepository.findByHostId(id).concatWith(gameRepository.findByGuestId(id));
  }
}
