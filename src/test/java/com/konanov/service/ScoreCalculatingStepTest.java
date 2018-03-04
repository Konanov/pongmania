package com.konanov.service;

import com.konanov.model.game.Game;
import com.konanov.model.game.Match;
import com.konanov.model.person.Player;
import com.konanov.model.person.Role;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static com.konanov.model.game.Match.Score.*;
import static com.konanov.model.person.Role.Name.ADMIN;
import static com.konanov.model.person.Role.Name.USER;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

@RunWith(SpringRunner.class)
public class ScoreCalculatingStepTest {

    private ScoreCalculatingStep scoreCalculatingStep;

    @Before
    public void setUp() throws Exception {
        scoreCalculatingStep = new ScoreCalculatingStep();
    }

    @Test
    public void shouldResolveGame() {
        Player host = getPlayer("johndoe@gmail.com","John Doe",
                "pass", singleton(new Role(ADMIN)));
        Player guest = getPlayer("petedammit@gmail.com","Pete Dammit",
                "password", singleton(new Role(USER)));
        Game game = new Game(new ObjectId());
        game.setHost(host);
        game.setGuest(guest);
        game.setMatches(getMatches(host, guest));

        Publisher<Game> gamePublisher = gameSubscriber -> {
            System.out.println("Subscribe to receive games");
            try {
                Random random = new Random();
                while (true) {
                    Thread.sleep(random.nextInt(20));
                    gameSubscriber.onNext(game);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Subscriber<ScoreCalculatingStep.FinalScore> scoreSubscriber = new Subscriber<ScoreCalculatingStep.FinalScore>() {
            @Override
            public void onSubscribe(Subscription s) {
                //System.out.println("Score subscriber is ready to consume");
                //s.request(1);
            }

            @Override
            public void onNext(ScoreCalculatingStep.FinalScore finalScore) {
                String result = String.format("Host score: %s, guest score: %s.", finalScore.getHostScore(), finalScore.getGuestScore());
                System.out.println(result);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Score Subscriber has ERROR");
            }

            @Override
            public void onComplete() {
                System.out.println("Score Subscriber completed");
            }
        };

        ScoreCalculatingStep scoreCalculatingStep = new ScoreCalculatingStep();
        scoreCalculatingStep.subscribe(scoreSubscriber);
        gamePublisher.subscribe(scoreCalculatingStep);

        //GameResolver.FinalScore score = gameResolver.resolveGame(game);
        //assertThat(score.getHostScore()).isLessThan(3);
        //assertThat(score.getGuestScore()).isLessThan(3);
        //assertThat(score.getHostStar()).isNull();
        //assertThat(score.getGuestStar()).isNull();
    }

    private Player getPlayer(String email, String username, String password, Set<Role> roles) {
        return new Player(new ObjectId(), new Player.Credentials(email, username, password, roles));
    }

    private Collection<Match> getMatches(Player host, Player guest) {
        Match first = getMatch(host, guest, ELEVEN, SIX);
        Match second = getMatch(host, guest, FOUR, ELEVEN);
        Match third = getMatch(host, guest, TEN, ELEVEN);
        return asList(first, second, third);
    }

    private Match getMatch(Player host, Player guest, Match.Score eleven, Match.Score six) {
        Match first = new Match(new ObjectId());
        Map<Player, Match.Score> firstScores = new HashMap<>();
        firstScores.put(host, eleven);
        firstScores.put(guest, six);
        first.setMatchResult(firstScores);
        return first;
    }
}