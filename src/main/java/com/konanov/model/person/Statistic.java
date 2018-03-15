package com.konanov.model.person;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "statistics")
public class Statistic {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private ObjectId playerUuid;
    private double matchWinRatio;
    private double gameWinRation;
    private double avarageScore;
    private long scoreSum;
    private LocalDateTime timestamp;
    private int gamesPlayed;
    private int matchesPlayed;
}
