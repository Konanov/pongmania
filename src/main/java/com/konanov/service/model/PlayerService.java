package com.konanov.service.model;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository repository;

    public Mono<Player> findByEmail(String email) {
        return repository.findByCredentials_Email(email);
    }

    public Mono<Player> insert(Player player) {
        return repository.save(player);
    }

    public Flux<Player> getPlayersOfLeague(PublicLeagueType type) {
        return repository.findByPublicLeague_TypeOrderByLatestRating_RatingDesc(type);
    }

    public Mono<Player> findById(ObjectId id) {
        return repository.findById(id);
    }

    public Flux<Player> retrieveAll() {
        return repository.findAll();
    }

    public Mono<Boolean> playerHasLeague( String email) {
        return repository.findByCredentials_Email(email)
                .flatMap(player -> Mono.just(player.getPublicLeague() != null));
    }

    public Mono<PublicLeague> playersPublicLeague(String email) {
        return repository.findByCredentials_Email(email).map(Player::getPublicLeague);
    }
}
