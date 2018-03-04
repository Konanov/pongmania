package com.konanov.endpoints;

import com.konanov.model.game.Game;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("game")
public class GameEndpoint implements Publisher<Game> {

    private Subscriber<? super Game> subscriber;

    @Override
    public void subscribe(Subscriber<? super Game> subscriber) {
        this.subscriber = subscriber;
    }

    @PostMapping("calculate")
    public void postGame(@RequestBody Game game) {
        subscriber.onNext(game);
    }
}
