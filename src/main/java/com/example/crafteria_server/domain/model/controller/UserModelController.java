package com.example.crafteria_server.domain.model.controller;

import com.example.crafteria_server.domain.model.dto.ModelPurchaseRequest;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.service.ModelService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j(topic = "UserModelController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/model/user")
@Tag(name = "도면 API - 사용자", description = "사용자 도면 관련 API")
public class UserModelController {
    private final ModelService modelService;

    @GetMapping("/list/popular")
    @Operation(summary = "도면 조회", description = "도면을 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getPopularModelList(@RequestParam(defaultValue = "0") int page,
                                                                          @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Optional<Long> userId = Optional.ofNullable(principalDetails).map(PrincipalDetails::getUserId);
        return JsonBody.of(200, "성공", modelService.getPopularList(page, userId));
    }

    @GetMapping("/view/{modelId}")
    @Operation(summary = "도면 상세 조회", description = "도면 상세 정보를 조회합니다.")
    public JsonBody<UserModelDto.ModelResponse> getModelDetail(@PathVariable Long modelId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Optional<Long> userId = Optional.ofNullable(principalDetails).map(PrincipalDetails::getUserId);
        UserModelDto.ModelResponse modelDetail = modelService.getModelDetail(modelId, userId);
        return JsonBody.of(200, "성공", modelDetail);
    }

    @GetMapping("/list/my")
    @Operation(summary = "내가 구매한 도면 조회", description = "내가 구매한 도면을 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getMyModelList(@RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.getMyDownloadedModelList(page, principalDetails.getUserId()));
    }

   /*@PostMapping("/purchase/{modelId}")
    @Operation(summary = "도면 구매", description = "도면을 구매합니다.")
    public JsonBody<UserModelDto.ModelResponse> purchaseModel(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long modelId) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return JsonBody.of(200, "성공", modelService.purchaseModel(principalDetails.getUserId(), modelId));
    }*/

    @PostMapping("/purchase/{modelId}")
    @Operation(summary = "도면 구매", description = "도면을 구매합니다.")
    public ResponseEntity<JsonBody<UserModelDto.ModelResponse>> purchaseModelWithCoupon(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody ModelPurchaseRequest request) {

        Long userId = principal.getUser().getId();
        UserModelDto.ModelResponse response = modelService.purchaseModelWithCoupon(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "도면 구매 성공", response));
    }

    @GetMapping("/list/popular-by-downloads")
    @Operation(summary = "다운로드 기준 인기 도면 조회", description = "downloadCount가 높은 순으로 도면을 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getPopularByDownloads(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Optional<Long> userId = Optional.ofNullable(principalDetails).map(PrincipalDetails::getUserId);
        return JsonBody.of(200, "성공", modelService.getPopularByDownloadList(page, userId));
    }

    // ✅ 무료 도면 리스트
    @GetMapping("/list/free")
    @Operation(summary = "무료 도면 조회", description = "price=0 인 도면을 최근 업로드 순으로 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getFreeModels(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Optional<Long> userId = Optional.ofNullable(principalDetails).map(PrincipalDetails::getUserId);
        return JsonBody.of(200, "성공", modelService.getFreeModelList(page, userId));
    }

    // ✅ 유료 도면 리스트
    @GetMapping("/list/paid")
    @Operation(summary = "유료 도면 조회", description = "price>0 인 도면을 최근 업로드 순으로 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getPaidModels(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Optional<Long> userId = Optional.ofNullable(principalDetails).map(PrincipalDetails::getUserId);
        return JsonBody.of(200, "성공", modelService.getPaidModelList(page, userId));
    }

    @GetMapping("/download/{modelId}")
    @Operation(summary = "모델 파일 다운로드", description = "모델의 STL이 여러 개면 ZIP으로, 하나면 원본 파일로 다운로드합니다.")
    public ResponseEntity<StreamingResponseBody> downloadModelFiles(
            @PathVariable Long modelId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long userId = principalDetails != null ? principalDetails.getUserId() : null;
        return modelService.downloadModelFiles(modelId, userId);
    }
}
