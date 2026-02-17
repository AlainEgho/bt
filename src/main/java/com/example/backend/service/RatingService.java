package com.example.backend.service;

import com.example.backend.dto.CreateRatingRequest;
import com.example.backend.dto.RatingResponse;
import com.example.backend.dto.UpdateRatingRequest;
import com.example.backend.entity.Item;
import com.example.backend.entity.Rating;
import com.example.backend.entity.User;
import com.example.backend.repository.ItemRepository;
import com.example.backend.repository.RatingRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<RatingResponse> findByItemId(String itemId) {
        return ratingRepository.findByItem_IdOrderByCreatedAtDesc(itemId).stream()
                .map(RatingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> findByUserId(Long userId) {
        return ratingRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(RatingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RatingResponse findById(Long id, Long userId) {
        Rating rating = ratingRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
        return RatingResponse.fromEntity(rating);
    }

    @Transactional
    public RatingResponse create(CreateRatingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setItem(item);
        rating.setRating(request.getRating());
        rating.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        rating = ratingRepository.save(rating);

        return RatingResponse.fromEntity(rating);
    }

    @Transactional
    public RatingResponse update(Long id, UpdateRatingRequest request, Long userId) {
        Rating rating = ratingRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));

        if (request.getRating() != null) {
            rating.setRating(request.getRating());
        }
        if (request.getDescription() != null) {
            rating.setDescription(request.getDescription().trim());
        }

        return RatingResponse.fromEntity(rating);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Rating rating = ratingRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
        ratingRepository.delete(rating);
    }
}
