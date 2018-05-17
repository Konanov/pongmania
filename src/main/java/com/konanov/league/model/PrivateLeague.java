package com.konanov.league.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.konanov.player.model.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Created by alex on 5/12/18.
 */
@Getter
@Setter
@RequiredArgsConstructor
@Document(collection = "private_leagues")
public class PrivateLeague {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private final String name;
    private Set<Player.Credentials> players;
}
