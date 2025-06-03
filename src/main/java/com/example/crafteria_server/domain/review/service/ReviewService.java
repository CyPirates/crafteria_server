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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "ReviewService")
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
        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getReview() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이 주문에 대한 리뷰가 이미 존재합니다.");
        }

        Manufacturer manufacturer = manufacturerRepository.findById(order.getManufacturer().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조업체를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        List<File> images = saveImages(requestDto.getImageFiles());

        Review review = Review.builder()
                .user(user)
                .manufacturer(manufacturer)
                .order(order)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .createdAt(LocalDateTime.now())
                .images(images)
                .build();

        review = reviewRepository.save(review);
        order.setReview(review);  // 주문에 리뷰 연결
        orderRepository.save(order);

        log.info("리뷰 작성: userId={}, orderId={}, manufacturerId={}, rating={}, content={}",
                userId, order.getId(), manufacturer.getId(), requestDto.getRating(), requestDto.getContent());

        return ReviewDto.ReviewResponseDto.from(review);
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
        review.setImages(newImages);

        String oldContent = review.getContent();
        int oldRating = review.getRating();

        review.setContent(requestDto.getContent());
        review.setRating(requestDto.getRating());
        reviewRepository.save(review);

        log.info("리뷰 수정: userId={}, reviewId={}, orderId={}, oldContent={}, newContent={}, oldRating={}, newRating={}",
                userId, review.getId(), review.getOrder().getId(), oldContent, requestDto.getContent(), oldRating, requestDto.getRating());

        return ReviewDto.ReviewResponseDto.from(review);
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다.");
        }

        Long orderId = review.getOrder().getId();

        fileService.deleteFiles(review.getImages());
        reviewRepository.delete(review);
        Manufacturer manufacturer = review.getManufacturer();
        if (manufacturer.getTotalReviews() > 0) {
            manufacturer.setTotalReviews(manufacturer.getTotalReviews() - 1);  // 리뷰 수 감소
            manufacturerRepository.save(manufacturer);
        }

        log.info("리뷰 삭제: userId={}, reviewId={}, orderId={}", userId, reviewId, orderId);

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

    public Page<ReviewDto.ReviewResponseDto> getReviewsByManufacturer(Long manufacturerId, Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByManufacturerId(manufacturerId, pageable);

        return reviewsPage.map(review -> ReviewDto.ReviewResponseDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .imageUrls(review.getImages().stream().map(File::getUrl).collect(Collectors.toList()))
                .build());
    }
}
