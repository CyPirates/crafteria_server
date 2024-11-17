package com.example.crafteria_server.domain.equipment.controller;

import com.example.crafteria_server.domain.equipment.dto.EquipmentDto;
import com.example.crafteria_server.domain.equipment.entity.Equipment;
import com.example.crafteria_server.domain.equipment.entity.EquipmentStatus;
import com.example.crafteria_server.domain.equipment.service.EquipmentService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j(topic = "EquipmentController")
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "장비", description = "장비 관련 API")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 장비 생성 (multipart/form-data로 요청 처리)
    @Operation(summary = "장비 등록", description = "장비 정보를 등록합니다.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<EquipmentDto.EquipmentResponse>> createEquipment(
            @ModelAttribute EquipmentDto.EquipmentRequest equipmentRequest) {

        EquipmentDto.EquipmentResponse response = equipmentService.createEquipment(equipmentRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "장비 생성 성공", response));
    }

    // 장비 수정 (multipart/form-data로 요청 처리)
    @Operation(summary = "장비 정보 수정", description = "장비 정보를 수정합니다.")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<JsonBody<EquipmentDto.EquipmentResponse>> updateEquipment(
            @PathVariable Long id,
            @ModelAttribute EquipmentDto.EquipmentRequest equipmentRequest) {

        EquipmentDto.EquipmentResponse response = equipmentService.updateEquipment(id, equipmentRequest);
        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "장비 수정 성공", response));
    }

    // 특정 장비 조회
    @Operation(summary = "특정 장비 조회", description = "ID를 통해 특정 장비의 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<JsonBody<EquipmentDto.EquipmentResponse>> getEquipmentById(@PathVariable Long id) {
        EquipmentDto.EquipmentResponse response = equipmentService.getEquipmentById(id);
        if (response == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)  // HTTP 상태 코드 404 설정
                    .body(JsonBody.of(404, "장비를 찾을 수 없음", null));
        }
        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "장비 조회 성공", response));
    }

    // 특정 제조사의 장비 목록 조회
    @Operation(summary = "특정 제조사의 모든 장비 조회", description = "특정 제조사가 보유한 모든 장비를 조회합니다.")
    @GetMapping("/manufacturer/{manufacturerId}")
    public ResponseEntity<JsonBody<List<EquipmentDto.EquipmentResponse>>> getEquipmentsByManufacturer(
            @PathVariable Long manufacturerId) {

        List<EquipmentDto.EquipmentResponse> responseList = equipmentService.getEquipmentsByManufacturer(manufacturerId);
        return ResponseEntity
                .status(HttpStatus.OK)  // HTTP 상태 코드 200 설정
                .body(JsonBody.of(200, "제조사 장비 조회 성공", responseList));
    }

    // 장비 삭제
    @Operation(summary = "장비 삭제", description = "장비 정보를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<JsonBody<Void>> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)  // HTTP 상태 코드 204 설정
                .body(JsonBody.of(204, "장비 삭제 성공", null));
    }

    // 장비 상태 변경 API 추가
    @Operation(summary = "장비 상태 변경", description = "장비 상태를 변경합니다.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<JsonBody<EquipmentDto.EquipmentResponse>> updateEquipmentStatus(
            @PathVariable Long id,
            @RequestParam EquipmentStatus status) {

        EquipmentDto.EquipmentResponse response = equipmentService.updateEquipmentStatus(id, status);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JsonBody.of(200, "장비 상태 수정 성공", response));
    }
}
