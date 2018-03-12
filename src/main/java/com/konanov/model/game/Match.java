package com.konanov.model.game;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.konanov.model.person.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@Document(collection = "matches")
public class Match {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId id;

    /**
     * Maps {@link Player} {@link ObjectId} to {@link Player} {@link Score} in current {@link Match}.
     * */
    private Map<String, Score> matchResult;

    @Getter
    @RequiredArgsConstructor
    public enum Score {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ELEVEN(11);

        private final int points;
    }
}
