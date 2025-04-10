package com.example.crafteria_server.domain.review.contoroller;

import com.example.crafteria_server.domain.review.dto.ReviewDto;
import com.example.crafteria_server.domain.review.service.ReviewService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "제조업체 리뷰 작성", description = "주문 완료된 제조업체에 대해 최대 3개의 이미지 파일을 첨부하여 리뷰를 작성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonBody<ReviewDto.ReviewResponseDto>> addReview(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute ReviewDto.ReviewRequestDto requestDto) {

        if (requestDto.getImageFiles() == null) {
            requestDto.setImageFiles(new ArrayList<>());
        }

        

        ReviewDto.ReviewResponseDto responseDto = reviewService.addReview(principalDetails.getUserId(), requestDto);
        return ResponseEntity.ok(JsonBody.of(200, "리뷰가 작성되었습니다.", responseDto));
    }

    @Operation(summary = "제조업체 리뷰 수정", description = "본인이 작성한 제조업체 리뷰를 최대 3개의 이미지 파일과 함께 수정합니다.")
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonBody<ReviewDto.ReviewResponseDto>> updateReview(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reviewId,
            @ModelAttribute ReviewDto.ReviewRequestDto requestDto) {

        if (requestDto.getImageFiles() == null) {
            requestDto.setImageFiles(new ArrayList<>());
        }

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

    @GetMapping("/manufacturer/{manufacturerId}")
    public ResponseEntity<JsonBody<Page<ReviewDto.ReviewResponseDto>>> getReviewsByManufacturer(
            @PathVariable Long manufacturerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDto.ReviewResponseDto> reviews = reviewService.getReviewsByManufacturer(manufacturerId, pageable);
        return ResponseEntity.ok(JsonBody.of(200, "성공적으로 조회되었습니다.", reviews));
    }
}
