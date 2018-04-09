package com.konanov.rating.service;

import com.konanov.rating.repository.RatingRepository;
import com.konanov.rating.model.Rating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;

    public Mono<Rating> latestRating(String uid) {
        return ratingRepository.findFirstByUidOrderByTimestampDesc(uid);
    }

    public Mono<Rating> insert(Rating rating) {
        return ratingRepository.insert(rating);
    }
}
