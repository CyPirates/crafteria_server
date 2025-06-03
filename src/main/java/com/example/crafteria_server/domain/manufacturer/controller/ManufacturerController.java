package com.example.crafteria_server.domain.manufacturer.controller;

import com.example.crafteria_server.domain.manufacturer.dto.ManufacturerDTO;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.service.ManufacturerService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@Slf4j(topic = "ManufacturerController")
@RequestMapping("/api/v1/manufacturers")
@RequiredArgsConstructor
@Tag(name = "제조사", description = "제조사 관련 API")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    // 제조사 등록
    @Operation(summary = "제조사 등록", description = "제조사 정보를 등록.")
    @PreAuthorize("hasRole('DASHBOARD')") // DASHBOARD 권한만 허용
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> createManufacturer(
            @ModelAttribute ManufacturerDTO.ManufacturerRequest manufacturerRequest,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException { // 현재 로그인된 유저 정보 가져오기

        ManufacturerDTO.ManufacturerResponse response =
                manufacturerService.createManufacturer(manufacturerRequest, principalDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 상태 코드 201 설정
                .body(JsonBody.of(201, "제조사 생성 성공", response));
    }

    // 특정 제조사 조회
    @Operation(summary = "특정 제조사 조회", description = "ID를 통해 특정 제조사의 정보를 조회.")
    @GetMapping("/{id}")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> getManufacturerById(@PathVariable Long id) {
        ManufacturerDTO.ManufacturerResponse response = manufacturerService.getManufacturerById(id);

        if (response == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)  // HTTP 상태 코드 404 설정
                    .body(JsonBody.of(404, "제조사 찾을 수 없음", null));
        }

        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "제조사 조회 성공", response));
    }

    // 모든 제조사 조회
    @Operation(summary = "모든 제조사 조회", description = "등록된 모든 제조사의 정보를 조회.")
    @GetMapping
    public ResponseEntity<JsonBody<List<ManufacturerDTO.ManufacturerResponse>>> getAllManufacturers() {
        List<ManufacturerDTO.ManufacturerResponse> responseList = manufacturerService.getAllManufacturers();
        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "모든 제조사 조회 성공", responseList));
    }

    // 제조사 수정
    @Operation(summary = "제조사 정보 수정", description = "제조사 정보를 수정.")
    @PreAuthorize("hasRole('DASHBOARD')") // 대시보드 권한만 허용
    @PutMapping(value = "{id}", consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> updateManufacturer(
            @PathVariable Long id,
            @ModelAttribute ManufacturerDTO.ManufacturerRequest manufacturerRequest,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {

        ManufacturerDTO.ManufacturerResponse response = manufacturerService.updateManufacturer(
                id, manufacturerRequest, principalDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JsonBody.of(200, "제조사 업데이트 성공", response));
    }


    // 제조사 삭제
    @Operation(summary = "제조사 삭제", description = "제조사의 정보를 삭제.")
    @PreAuthorize("hasRole('DASHBOARD')") // 대시보드 권한만 허용
    @DeleteMapping("/{id}")
    public ResponseEntity<JsonBody<Void>> deleteManufacturer(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {

        manufacturerService.deleteManufacturer(id, principalDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(JsonBody.of(204, "제조사 삭제 성공", null));
    }

    // 제조사 상세 설명 업데이트
    @Operation(summary = "제조사 상세 설명 업데이트", description = "제조사의 상세 설명과 이미지를 업데이트합니다.")
    @PreAuthorize("hasRole('DASHBOARD')") // DASHBOARD 권한만 허용
    @PostMapping(value="/{id}/details",consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> updateManufacturerDetails(
            @PathVariable Long id,
            @ModelAttribute ManufacturerDTO.DetailedDescriptionRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {

        ManufacturerDTO.ManufacturerResponse response = manufacturerService.updateManufacturerDetails(id, request, principalDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JsonBody.of(200, "제조사 상세 설명 업데이트 성공", response));
    }

    @Operation(summary = "제조사 상세 정보 조회", description = "제조사의 상세 정보를 조회합니다.")
    @GetMapping("/{id}/details")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerDetailsResponse>> getManufacturerDetails(@PathVariable Long id) {
        ManufacturerDTO.ManufacturerDetailsResponse details = manufacturerService.getManufacturerDetailsById(id);

        if (details == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(JsonBody.of(404, "제조사를 찾을 수 없습니다.", null));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JsonBody.of(200, "상세 설명 조회 성공", details));
    }
}
