package com.konanov.player.service;

import com.konanov.player.repository.PlayerRepository;
import com.konanov.player.model.Player;
import com.konanov.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PlayerRepository repository;

    /**
     * Searches {@link Player} by {@literal email}, since {@literal userName} is not unique.
     *
     * @param email - email to search {@link Player} by.
     * */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //TODO Refactor whole thing to Reactive Security.
        return repository.findByCredentials_Email(email)
                .doOnError((e) -> new PongManiaException(String.format("No user found for email %s", email)))
                .block();
    }
}
