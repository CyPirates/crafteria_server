package com.example.crafteria_server.domain.manufacturer.controller;

import com.example.crafteria_server.domain.manufacturer.dto.ManufacturerDTO;
import com.example.crafteria_server.domain.manufacturer.service.ManufacturerService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> createManufacturer(
            @ModelAttribute ManufacturerDTO.ManufacturerRequest manufacturerRequest) {

        ManufacturerDTO.ManufacturerResponse response = manufacturerService.createManufacturer(manufacturerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)  // HTTP 상태 코드 201 설정
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

    // 제조사 업데이트
    @Operation(summary = "제조사 정보 수정", description = "제조사 정보를 수정.")
    @PutMapping("/{id}")
    public ResponseEntity<JsonBody<ManufacturerDTO.ManufacturerResponse>> updateManufacturer(
            @PathVariable Long id,
            @ModelAttribute ManufacturerDTO.ManufacturerRequest manufacturerRequest) {

        ManufacturerDTO.ManufacturerResponse response = manufacturerService.updateManufacturer(id, manufacturerRequest);
        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "제조사 업데이트 성공", response));
    }

    // 제조사 삭제
    @Operation(summary = "제조사 삭제", description = "제조사의 정보를 삭제.")
    @DeleteMapping("/{id}")
    public ResponseEntity<JsonBody<Void>> deleteManufacturer(@PathVariable Long id) {
        manufacturerService.deleteManufacturer(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)  // HTTP 상태 코드 204 설정
                .body(JsonBody.of(204, "제조사 삭제 성공", null));
    }
}
