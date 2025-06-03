package com.example.crafteria_server.domain.technology.contoller;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.technology.dto.TechnologyDto;
import com.example.crafteria_server.domain.technology.entity.Technology;
import com.example.crafteria_server.domain.technology.service.TechnologyService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/technologies")
@RequiredArgsConstructor
public class TechnologyController {
    private final TechnologyService technologyService;
    private final FileService fileService;
    private final ManufacturerRepository manufacturerRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "기술 생성", description = "기술을 생성합니다.")
    public ResponseEntity<JsonBody<TechnologyDto.TechnologyResponse>> createTechnology(@ModelAttribute TechnologyDto.TechnologyRequest technologyRequest) throws IOException {
        Technology technology = technologyService.createTechnology(technologyRequest, fileService, manufacturerRepository);
        TechnologyDto.TechnologyResponse response = TechnologyDto.TechnologyResponse.from(technology);
        return ResponseEntity.ok(JsonBody.of(200, "Technology created successfully", response));
    }

    @PutMapping(value = "/{technologyId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "기술 수정", description = "기술을 수정합니다.")
    public ResponseEntity<JsonBody<TechnologyDto.TechnologyResponse>> updateTechnology(@PathVariable Long technologyId, @ModelAttribute TechnologyDto.TechnologyRequest technologyRequest) throws IOException {
        Technology updatedTechnology = technologyService.updateTechnology(technologyId, technologyRequest, fileService);
        TechnologyDto.TechnologyResponse response = TechnologyDto.TechnologyResponse.from(updatedTechnology);
        return ResponseEntity.ok(JsonBody.of(200, "Technology updated successfully", response));
    }

    @DeleteMapping(value = "/{technologyId}")
    @Operation(summary = "기술 삭제", description = "기술을 삭제합니다.")
    public ResponseEntity<JsonBody<Void>> deleteTechnology(@PathVariable Long technologyId) {
        technologyService.deleteTechnology(technologyId);
        return ResponseEntity.ok(JsonBody.of(200, "Technology deleted successfully", null));
    }

    @GetMapping(value = "/manufacturer/{manufacturerId}")
    @Operation(summary = "제조사별 기술 조회", description = "제조사별 기술을 조회합니다.")
    public ResponseEntity<JsonBody<List<TechnologyDto.TechnologyResponse>>> getTechnologiesByManufacturer(@PathVariable Long manufacturerId) {
        List<Technology> technologies = technologyService.getAllTechnologiesByManufacturer(manufacturerId);
        List<TechnologyDto.TechnologyResponse> responses = technologies.stream().map(TechnologyDto.TechnologyResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(JsonBody.of(200, "Technologies retrieved successfully", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "기술 조회", description = "주어진 ID로 특정 기술을 조회합니다.")
    public ResponseEntity<JsonBody<TechnologyDto.TechnologyResponse>> getTechnologyById(@PathVariable Long id) {
        TechnologyDto.TechnologyResponse response = technologyService.getTechnologyById(id);
        return ResponseEntity.ok(JsonBody.of(200, "Technology retrieved successfully", response));
    }
}