package com.konanov.model.person;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.konanov.gliko.Rating;
import com.konanov.model.game.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Setter
@Getter
@Document(collection = "players")
@NoArgsConstructor
public class Player implements UserDetails {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private Credentials credentials;
    private Status status;
    private Rating rating;
    private double points;
    private Star star;
    private Collection<Game> games;
    private Collection<GrantedAuthority> authorities;

    public Player(ObjectId id, Credentials credentials, Rating rating, String[] authorities) {
        this.id = id;
        this.credentials = credentials;
        this.authorities = AuthorityUtils.createAuthorityList(authorities);
        this.rating = rating;
    }

    @Override
    public String getPassword() {
        return this.credentials.getPassword();
    }

    @Override
    public String getUsername() {
        return this.credentials.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credentials {
        private String email;
        private String userName;
        private String password;
    }

    @Getter
    private enum Status {
        ACTIVE, PASSIVE
    }

    @Getter
    @RequiredArgsConstructor
    public enum Star {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

        private final int number;
    }
}
