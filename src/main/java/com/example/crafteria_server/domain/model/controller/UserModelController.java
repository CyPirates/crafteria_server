package com.example.crafteria_server.domain.model.controller;

import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.service.ModelService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Long userId = principalDetails.getUserId();
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

    @PostMapping("/purchase/{modelId}")
    @Operation(summary = "도면 구매", description = "도면을 구매합니다.")
    public JsonBody<UserModelDto.ModelResponse> purchaseModel(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long modelId) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.purchaseModel(principalDetails.getUserId(), modelId));
    }
}
