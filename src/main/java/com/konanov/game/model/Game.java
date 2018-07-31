package com.konanov.game.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId hostId;
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId guestId;
  private String hostEmail;
  private String guestEmail;
  private Type type;
  private Boolean approved;
  @JsonFormat(pattern = "dd-MM-yyyy")
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate gameDate;
  @JsonDeserialize(using = LocalTimeDeserializer.class)
  private LocalTime gameTime;
  private LocalDateTime createdAt;

  @Getter
  private enum Type {
    SHORT,
    LONG
  }
}
