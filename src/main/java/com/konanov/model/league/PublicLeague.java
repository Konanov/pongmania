package com.konanov.model.league;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "public_leagues")
@AllArgsConstructor
@NoArgsConstructor
public class PublicLeague {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String type;
}
