package com.konanov.service.model;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.repository.PublicLeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final PlayerRepository playerRepository;
    private final PublicLeagueRepository leagueRepository;

    public Flux<Player> playersFromLeague(String email) {
        return playerRepository.findByCredentials_Email(email)
                .flatMapMany(player -> {
                    Optional<PublicLeague> league = Optional.ofNullable(player.getPublicLeague());
                    if (league.isPresent()) {
                        return playerRepository.findByPublicLeague_Type(league.get().getType());
                    }
                    return Flux.empty();
                });
    }

    public Mono<Long> countLeaguePlayers(String email) {
        return playerRepository.findByCredentials_Email(email)
                .flatMap(player -> {
                    Optional<PublicLeague> league = Optional.ofNullable(player.getPublicLeague());
                    if (league.isPresent()) {
                        return playerRepository.countByPublicLeague_Type(league.get().getType());
                    }
                    return Mono.empty();
                }).switchIfEmpty(Mono.just(0L));
    }

    public Mono<PublicLeague> findByType(PublicLeagueType type) {
        return leagueRepository.findByType(type);
    }
}
