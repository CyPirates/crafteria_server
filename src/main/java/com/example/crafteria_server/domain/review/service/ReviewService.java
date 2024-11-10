package com.example.crafteria_server.domain.review.service;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.example.crafteria_server.domain.review.dto.ReviewDto;
import com.example.crafteria_server.domain.review.entity.Review;
import com.example.crafteria_server.domain.review.repository.ReviewRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final UserRepository userRepository;

    public ReviewDto.ReviewResponseDto addReview(Long userId, ReviewDto.ReviewRequestDto requestDto) {
        Manufacturer manufacturer = manufacturerRepository.findById(requestDto.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조업체를 찾을 수 없습니다."));

        // 주문 완료 여부 확인
        orderRepository.findByUserIdAndManufacturerIdAndStatus(userId, requestDto.getManufacturerId(), OrderStatus.DELIVERED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 제조업체에 주문 완료된 유저만 리뷰를 남길 수 있습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Review review = Review.builder()
                .user(user)
                .manufacturer(manufacturer)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
        updateManufacturerRating(manufacturer);

        return new ReviewDto.ReviewResponseDto(review.getId(), review.getContent(), review.getRating(), review.getCreatedAt());
    }

    public ReviewDto.ReviewResponseDto updateReview(Long userId, Long reviewId, ReviewDto.ReviewRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
        }

        review.setContent(requestDto.getContent());
        review.setRating(requestDto.getRating());
        reviewRepository.save(review);
        updateManufacturerRating(review.getManufacturer());

        return new ReviewDto.ReviewResponseDto(review.getId(), review.getContent(), review.getRating(), review.getCreatedAt());
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다.");
        }

        Manufacturer manufacturer = review.getManufacturer();
        reviewRepository.delete(review);
        updateManufacturerRating(manufacturer);
    }

    public List<ReviewDto.ReviewResponseDto> getReviewsByManufacturer(Long manufacturerId) {
        Manufacturer manufacturer = manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조업체를 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findByManufacturer(manufacturer);

        return reviews.stream()
                .map(review -> new ReviewDto.ReviewResponseDto(
                        review.getId(),
                        review.getContent(),
                        review.getRating(),
                        review.getCreatedAt()))
                .toList();
    }

    private void updateManufacturerRating(Manufacturer manufacturer) {
        List<Review> reviews = reviewRepository.findByManufacturer(manufacturer);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(5.0);

        manufacturer.setRating((int) Math.round(averageRating));
        manufacturerRepository.save(manufacturer);
    }
}
