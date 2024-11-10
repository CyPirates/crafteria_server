package com.example.crafteria_server.domain.review.contoroller;

import com.example.crafteria_server.domain.review.dto.ReviewDto;
import com.example.crafteria_server.domain.review.service.ReviewService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "제조업체 리뷰 작성", description = "주문 완료된 제조업체에 대해 리뷰를 작성합니다.")
    @PostMapping
    public ResponseEntity<JsonBody<ReviewDto.ReviewResponseDto>> addReview(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody ReviewDto.ReviewRequestDto requestDto) {

        ReviewDto.ReviewResponseDto responseDto = reviewService.addReview(principalDetails.getUserId(), requestDto);
        return ResponseEntity.ok(JsonBody.of(200, "리뷰가 작성되었습니다.", responseDto));
    }

    @Operation(summary = "제조업체 리뷰 수정", description = "본인이 작성한 제조업체 리뷰를 수정합니다.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<JsonBody<ReviewDto.ReviewResponseDto>> updateReview(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reviewId,
            @RequestBody ReviewDto.ReviewRequestDto requestDto) {

        ReviewDto.ReviewResponseDto responseDto = reviewService.updateReview(principalDetails.getUserId(), reviewId, requestDto);
        return ResponseEntity.ok(JsonBody.of(200, "리뷰가 수정되었습니다.", responseDto));
    }

    @Operation(summary = "제조업체 리뷰 삭제", description = "본인이 작성한 제조업체 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<JsonBody<Void>> deleteReview(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(principalDetails.getUserId(), reviewId);
        return ResponseEntity.ok(JsonBody.of(200, "리뷰가 삭제되었습니다.", null));
    }

    @Operation(summary = "제조업체의 모든 리뷰 조회", description = "특정 제조업체에 대한 모든 리뷰를 조회합니다.")
    @GetMapping("/manufacturer/{manufacturerId}")
    public ResponseEntity<JsonBody<List<ReviewDto.ReviewResponseDto>>> getReviewsByManufacturer(@PathVariable Long manufacturerId) {
        List<ReviewDto.ReviewResponseDto> reviews = reviewService.getReviewsByManufacturer(manufacturerId);
        return ResponseEntity.ok(JsonBody.of(200, "성공적으로 조회되었습니다.", reviews));
    }
}
