package com.example.crafteria_server.domain.model.controller;

import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.service.ModelService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Slf4j(topic = "UserModelController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/model/user")
@Tag(name = "도면 API - 사용자", description = "사용자 도면 관련 API")
public class UserModelController {
    private final ModelService modelService;

    @GetMapping("/list/popular")
    public JsonBody<List<UserModelDto.ModelResponse>> getPopularModelList(@RequestParam(defaultValue = "0") int page) {
        return JsonBody.of(200, "성공", modelService.getPopularList(page));
    }

    @GetMapping("/view/{modelId}")
    public JsonBody<UserModelDto.ModelResponse> getModelDetail(@PathVariable Long modelId) {
        return JsonBody.of(200, "성공", modelService.getModelDetail(modelId));
    }

    @GetMapping("/list/my")
    public JsonBody<List<UserModelDto.ModelResponse>> getMyModelList(@RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.getMyDownloadedModelList(page, principalDetails.getUserId()));
    }

    @PostMapping("/purchase/{modelId}")
    public JsonBody<UserModelDto.ModelResponse> purchaseModel(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long modelId) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.purchaseModel(principalDetails.getUserId(), modelId));
    }
}
