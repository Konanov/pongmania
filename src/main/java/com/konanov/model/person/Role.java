package com.konanov.model.person;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Setter
@Getter
@Document
@NoArgsConstructor
public class Role {
    private ObjectId id;
    private Name name;
    private Set<Player> players;

    public Role(Name name) {
        this.name = name;
    }

    @Getter
    @NoArgsConstructor
    public enum Name {
        ADMIN("ADMIN"), USER("USER");

        Name(String value) {
            this.value = value;
        }

        private String value;
    }
}
