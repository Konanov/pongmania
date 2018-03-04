package com.konanov.model.game;

import com.konanov.model.person.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

@Setter
@Getter
@RequiredArgsConstructor
@Document(collection = "games")
public class Game {
    @Id
    private final ObjectId id;
    private Collection<Match> matches;
    private Player host;
    private Player guest;
    private Type type;
    private Boolean approved;

    @Getter
    private enum Type {
        SHORT, LONG
    }
}
