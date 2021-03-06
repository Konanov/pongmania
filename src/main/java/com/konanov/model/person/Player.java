package com.konanov.model.person;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.konanov.gliko.Rating;
import com.konanov.model.game.Game;
import com.konanov.model.league.League;
import com.konanov.model.league.PublicLeague;
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
    private double points;
    private Star star;
    private Rating latestRating;
    private PublicLeague publicLeague;
    private Collection<Game> games;
    private Collection<League> privateLeagues;
    private Collection<GrantedAuthority> authorities;

    public Player(ObjectId id, Credentials credentials, String[] authorities) {
        this.id = id;
        this.credentials = credentials;
        this.authorities = AuthorityUtils.createAuthorityList(authorities);
    }

    @Override
    public String getPassword() {
        return this.credentials.getPassword();
    }

    @Override
    public String getUsername() {
        return String.format("%s %s", this.credentials.getFirstName(), this.credentials.getLastName());
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
        private String firstName;
        private String lastName;
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
