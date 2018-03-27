package com.konanov.service;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import com.konanov.model.person.Player;
import com.konanov.repository.PlayerRepository;
import com.konanov.repository.PublicLeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final PlayerRepository playerRepository;
    private final PublicLeagueRepository leagueRepository;

    public Flux<Player> playersFromLeague(String email) {
        return playerRepository.findByCredentials_Email(email)
                .flatMapMany(player -> {
                    PublicLeagueType type = player.getPublicLeague().getType();
                    return playerRepository.findByPublicLeague_Type(type);
                });
    }

    public Mono<Long> countLeaguePlayers(String email) {
        return playerRepository.findByCredentials_Email(email)
                .flatMap(player -> {
                    PublicLeagueType type = player.getPublicLeague().getType();
                    return playerRepository.countByPublicLeague_Type(type);
                });
    }

    public Mono<PublicLeague> findByType(PublicLeagueType type) {
        return leagueRepository.findByType(type);
    }
}
