package com.example.crafteria_server.domain.review.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.order.entity.Order;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public ReviewDto.ReviewResponseDto addReview(Long userId, ReviewDto.ReviewRequestDto requestDto) {
        Manufacturer manufacturer = manufacturerRepository.findById(requestDto.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조업체를 찾을 수 없습니다."));

        List<Order> orders = orderRepository.findByUserIdAndManufacturerIdAndStatus(
                userId, requestDto.getManufacturerId(), OrderStatus.DELIVERED);

        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 제조업체에 주문 완료된 유저만 리뷰를 남길 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        List<File> images = saveImages(requestDto.getImageFiles());


        Review review = Review.builder()
                .user(user)
                .manufacturer(manufacturer)
                .images(images)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
        manufacturer.setTotalReviews(manufacturer.getTotalReviews() + 1);  // 리뷰 수 증가
        manufacturerRepository.save(manufacturer);
        updateManufacturerRating(manufacturer);

        return new ReviewDto.ReviewResponseDto(
                review.getId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                images.stream().map(File::getUrl).collect(Collectors.toList())
        );
    }

    public ReviewDto.ReviewResponseDto updateReview(Long userId, Long reviewId, ReviewDto.ReviewRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
        }

        // 기존 이미지 파일 삭제
        fileService.deleteFiles(review.getImages());

        // 새 이미지 처리
        List<File> newImages = saveImages(requestDto.getImageFiles());
        review.getImages().clear();  // 기존 이미지 리스트를 비웁니다.
        review.getImages().addAll(newImages);  // 새 이미지 리스트를 추가합니다.

        review.setContent(requestDto.getContent());
        review.setRating(requestDto.getRating());
        reviewRepository.save(review);
        updateManufacturerRating(review.getManufacturer());

        return new ReviewDto.ReviewResponseDto(
                review.getId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                newImages.stream().map(File::getUrl).collect(Collectors.toList()) // 이미지 URI 리스트
        );
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다.");
        }

        fileService.deleteFiles(review.getImages());
        reviewRepository.delete(review);
        Manufacturer manufacturer = review.getManufacturer();
        if (manufacturer.getTotalReviews() > 0) {
            manufacturer.setTotalReviews(manufacturer.getTotalReviews() - 1);  // 리뷰 수 감소
            manufacturerRepository.save(manufacturer);
        }
        updateManufacturerRating(review.getManufacturer());
    }

    private List<File> saveImages(List<MultipartFile> imageFiles) {
        // 이미지 파일이 없을 경우 빈 리스트를 반환
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        if (imageFiles.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일은 최대 3개까지 첨부할 수 있습니다.");
        }

        return imageFiles.stream()
                .map(fileService::saveImage)
                .collect(Collectors.toList());
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

    public List<ReviewDto.ReviewResponseDto> getReviewsByManufacturer(Long manufacturerId) {
        Manufacturer manufacturer = manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조업체를 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findByManufacturer(manufacturer);

        return reviews.stream()
                .map(review -> new ReviewDto.ReviewResponseDto(
                        review.getId(),
                        review.getContent(),
                        review.getRating(),
                        review.getCreatedAt(),
                        review.getImages().stream().map(File::getUrl).collect(Collectors.toList()) // 이미지 URI 리스트
                ))
                .collect(Collectors.toList());
    }
}
