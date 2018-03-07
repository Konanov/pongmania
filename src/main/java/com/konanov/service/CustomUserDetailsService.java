package com.konanov.service;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.exceptions.PongManiaException;
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
        return repository.findByCredentials_Email(email)
        .orElseThrow(() -> new PongManiaException(String.format("No user found for email %s", email)));
    }
}
