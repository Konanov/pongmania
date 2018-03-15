package com.konanov.model.person;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "statistics")
public class Statistic {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private ObjectId playerUuid;
    private double matchWinRatio;
    private double averageScore;
    private double scoreSum;
    private LocalDateTime timestamp;
    private long gamesPlayed;
    private long matchesPlayed;
}
