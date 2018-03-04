package com.konanov.model.person;

import com.konanov.model.game.Game;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Set;

@Setter
@Getter
@Document(collection = "players")
@NoArgsConstructor
public class Player {

    @Id
    private ObjectId id;
    private Credentials credentials;
    private Status status;
    private double points;
    private Star star;
    private Collection<Game> games;

    public Player(ObjectId id, Credentials credentials) {
        this.id = id;
        this.credentials = credentials;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credentials {
        private String email;
        private String userName;
        private String password;
        private Set<Role> roles;
    }

    @Getter
    private enum Status {
        ACTIVE, PASSIVE
    }

    @Getter
    @RequiredArgsConstructor
    public enum Star {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

        private final int number;
    }
}
