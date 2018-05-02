/*
package com.konanov.game.service;

import static java.time.ZonedDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.konanov.game.model.Game;
import com.konanov.game.repository.GameRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
class GameServiceTest {

  @Mock
  private GameRepository repository;

  private GameService gameService;

  private ObjectId id;
  private Game firstAsHost;
  private Game secondAsHost;
  private Game firstAsGuest;
  private Game plannedAsHost;
  private Game plannedAsGuest;

  public GameServiceTest() {
    MockitoAnnotations.initMocks(this);
    gameService = new GameService(repository);
  }

  @BeforeEach
  void setUp() {
    id = new ObjectId();
    firstAsHost = new Game();
    firstAsHost.setHostId(id);
    firstAsHost.setApproved(true);
    firstAsHost.setPlanedGameDate(now().minusDays(5));
    firstAsGuest = new Game();
    firstAsGuest.setGuestId(id);
    firstAsGuest.setApproved(true);
    firstAsGuest.setPlanedGameDate(now().minusDays(2));
    secondAsHost = new Game();
    secondAsHost.setHostId(id);
    secondAsHost.setApproved(true);
    secondAsHost.setPlanedGameDate(now().minusDays(1));
    plannedAsHost = new Game();
    plannedAsHost.setHostId(id);
    plannedAsHost.setApproved(false);
    plannedAsHost.setPlanedGameDate(now().plusDays(1));
    plannedAsGuest = new Game();
    plannedAsGuest.setGuestId(id);
    plannedAsGuest.setApproved(false);
    plannedAsGuest.setPlanedGameDate(now().plusDays(3));
  }

  @Test
  void shouldCountPlayerPlannedGames() {
    when(repository.countByHostIdAndApprovedAndPlanedGameDateIsGreaterThan(id, false, now()))
        .thenReturn(Mono.just(1L));
    when(repository.countByGuestIdAndApprovedAndPlanedGameDateIsGreaterThan(id, false, now()))
        .thenReturn(Mono.just(1L));
    assertEquals(gameService.countPlanedGames(id).block().get(id).longValue(), 2L);
  }

  @Test
  void shouldCountPlayerPlayedGames() {
    when(repository.countByHostIdAndApprovedAndPlanedGameDateLessThan(id, true, now()))
        .thenReturn(Mono.just(2L));
    when(repository.countByGuestIdAndApprovedAndPlanedGameDateLessThan(id, true, now()))
        .thenReturn(Mono.just(1L));
    assertEquals(gameService.countPlayedGames(id).block().get(id).longValue(), 3L);
  }

  @Test
  void shouldFindAllUserGames() {
    when(repository.findByHostId(id)).thenReturn(Flux.just(firstAsHost, secondAsHost));
    when(repository.findByGuestId(id)).thenReturn(Flux.just(firstAsGuest));
    Flux<Game> games = gameService.findAllPlayerGames(id);
    assertEquals(games.count().block(),
        Flux.just(firstAsGuest, firstAsHost, firstAsGuest).count().block());
  }
}*/
