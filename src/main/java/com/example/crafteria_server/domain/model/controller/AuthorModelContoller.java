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
@Slf4j(topic = "ManufacturerModelController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/model/author")
@Tag(name = "도면 API - 작가", description = "작가 도면 관련 API")
public class AuthorModelContoller {
    private final ModelService modelService;

    // 내가 올린 도면 조회
    @GetMapping("/list/my")
    public JsonBody<List<UserModelDto.ModelResponse>> getMyModelList(@RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.getMyUploadedModelList(page, principalDetails.getUserId()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data", produces = "application/json")
    public JsonBody<UserModelDto.ModelResponse> uploadModel(@AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute UserModelDto.ModelUploadRequest request) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.uploadModel(principalDetails.getUserId(), request));
    }
}
