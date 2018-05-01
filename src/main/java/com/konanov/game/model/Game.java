package com.konanov.game.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.ZonedDateTime;
import java.util.Collection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@Document(collection = "games")
public class Game {

  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  private Collection<Match> matches;
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId hostId;
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId guestId;
  private String hostEmail;
  private String guestEmail;
  private Type type;
  private Boolean approved;
  private String gameDate;
  private String gameTime;
  private ZonedDateTime createdAt;

  @Getter
  private enum Type {
    SHORT,
    LONG
  }
}
