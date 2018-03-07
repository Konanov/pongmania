package com.konanov.model.game;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Collection;

@Setter
@Getter
@NoArgsConstructor
@Document(collection = "games")
public class Game {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private Collection<Match> matches;
    private String hostEmail;
    private String guestEmail;
    private Type type;
    private Boolean approved;
    private ZonedDateTime planedGameDate;

    @Getter
    private enum Type {
        SHORT, LONG
    }
}
