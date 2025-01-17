package com.example.crafteria_server.domain.search.controller;

import com.example.crafteria_server.domain.search.dto.SearchDto;
import com.example.crafteria_server.domain.search.service.SearchService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    // 제조사 검색
    @GetMapping("/manufacturers/by-name")
    @Operation(summary = "제조사 이름 검색", description = "제조사 이름으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchManufacturersByName(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchManufacturersByName(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "제조사 이름으로 검색 성공", results));
    }

    @GetMapping("/manufacturers/by-description")
    @Operation(summary = "제조사 설명 검색", description = "제조사 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchManufacturersByDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchManufacturersByDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "제조사 설명으로 검색 성공", results));
    }

    @GetMapping("/manufacturers/by-name-description")
    @Operation(summary = "제조사 이름과 설명 검색", description = "제조사 이름과 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchManufacturersByNameAndDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchManufacturersByNameAndDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "제조사 이름과 설명으로 검색 성공", results));
    }

    // 작가 검색
    @GetMapping("/authors/by-name")
    @Operation(summary = "작가 이름 검색", description = "작가 이름으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchAuthorsByName(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchAuthorsByName(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "작가 이름으로 검색 성공", results));
    }

    @GetMapping("/authors/by-description")
    @Operation(summary = "작가 설명 검색", description = "작가 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchAuthorsByDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchAuthorsByDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "작가 설명으로 검색 성공", results));
    }

    @GetMapping("/authors/by-name-description")
    @Operation(summary = "작가 이름과 설명 검색", description = "작가 이름과 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchAuthorsByNameAndDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchAuthorsByNameAndDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "작가 이름과 설명으로 검색 성공", results));
    }

    // 모델 검색
    @GetMapping("/models/by-name")
    @Operation(summary = "모델 이름 검색", description = "모델 이름으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchModelsByName(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchModelsByName(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "모델 이름으로 검색 성공", results));
    }

    @GetMapping("/models/by-description")
    @Operation(summary = "모델 설명 검색", description = "모델 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchModelsByDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchModelsByDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "모델 설명으로 검색 성공", results));
    }

    @GetMapping("/models/by-name-description")
    @Operation(summary = "모델 이름과 설명 검색", description = "모델 이름과 설명으로 검색합니다.")
    public ResponseEntity<JsonBody<List<SearchDto.SearchResultDto>>> searchModelsByNameAndDescription(@RequestParam String keyword) {
        List<SearchDto.SearchResultDto> results = searchService.searchModelsByNameAndDescription(keyword);
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonBody.of(201, "모델 이름과 설명으로 검색 성공", results));
    }
}
