package com.example.crafteria_server.domain.model.controller;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
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

@RestController
@Slf4j(topic = "ManufacturerModelController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/model/author")
@Tag(name = "도면 API - 작가", description = "작가 도면 관련 API")
public class AuthorModelContoller {
    private final ModelService modelService;
    private final ModelRepository modelRepository;
    private final FileService fileService;
    // 내가 올린 도면 조회
    @GetMapping("/list/my")
    @Operation(summary = "내가 올린 도면 조회", description = "내가 올린 도면을 조회합니다.")
    public JsonBody<List<UserModelDto.ModelResponse>> getMyModelList(@RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.getMyUploadedModelList(page, principalDetails.getUserId()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data", produces = "application/json")
    @Operation(summary = "도면 업로드", description = "도면을 업로드합니다.")
    public JsonBody<UserModelDto.ModelResponse> uploadModel(@AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute UserModelDto.ModelUploadRequest request) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return JsonBody.of(200, "성공", modelService.uploadModel(principalDetails.getUserId(), request));
    }

    // 도면 수정
    @PutMapping(value = "/update/{modelId}", consumes = "multipart/form-data", produces = "application/json")
    @Operation(summary = "도면 수정", description = "도면을 수정합니다.")
    public JsonBody<UserModelDto.ModelResponse> updateModel(@PathVariable Long modelId, @AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute UserModelDto.ModelUploadRequest request) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return JsonBody.of(200, "성공", modelService.updateModel(modelId, principalDetails.getUserId(), request));
    }

    // 도면 삭제
    @DeleteMapping("/delete/{modelId}")
    @Operation(summary = "도면 삭제", description = "도면을 삭제합니다.")
    public JsonBody<Void> deleteModel(@PathVariable Long modelId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        modelService.deleteModel(modelId, principalDetails.getUserId());
        return JsonBody.of(200, "도면이 삭제되었습니다.", null);
    }
}
