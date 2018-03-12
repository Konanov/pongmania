package com.konanov.service.model;

import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.service.exceptions.PongManiaException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;

    public Mono<Player> findByEmail(String email) {
        return playerRepository.findByCredentials_Email(email);
    }

    public Mono<Player> insert(Player player) {
        return playerRepository.insert(player);
    }

    public Mono<Player> findById(ObjectId id) {
        return playerRepository.findById(id);
    }
}
