package com.konanov.service;

import com.konanov.model.person.Player;
import com.konanov.model.person.Role;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PlayerRepository repository;

    /**
     * Searches {@link Player} by {@literal email}, since {@literal userName} is not unique.
     *
     * @param email - email to search {@link Player} by.
     * */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByCredentials_Email(email).map(player -> {
            final Player.Credentials credentials = player.getCredentials();
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            for (Role role : credentials.getRoles()){
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getName().getValue()));
            }
            return new User(credentials.getUserName(), credentials.getPassword(), grantedAuthorities);
        }).orElseThrow(() -> new PongManiaException(String.format("No user found for email %s", email)));
    }
}
