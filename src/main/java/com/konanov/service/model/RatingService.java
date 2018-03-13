package com.konanov.service.model;

import com.konanov.gliko.Rating;
import com.konanov.repository.RatingRepository;
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
