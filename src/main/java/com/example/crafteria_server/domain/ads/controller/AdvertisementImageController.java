package com.example.crafteria_server.domain.ads.controller;

import com.example.crafteria_server.domain.ads.dto.AdvertisementImageDto;
import com.example.crafteria_server.domain.ads.entity.AdvertisementImage;
import com.example.crafteria_server.domain.ads.service.AdvertisementImageService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/advertisement-images")
@RequiredArgsConstructor
@Slf4j(topic = "AdvertisementImageController")
public class AdvertisementImageController {

    private final AdvertisementImageService advertisementImageService;

    /**
     * 광고 이미지 업로드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "광고 이미지 업로드", description = "새로운 광고 이미지를 업로드합니다.")
    public ResponseEntity<JsonBody<AdvertisementImage>> uploadAdvertisementImage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {

        AdvertisementImage advertisementImage = advertisementImageService.uploadAdvertisementImage(imageFile, title, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "광고 이미지 업로드 성공", advertisementImage));
    }

    /**
     * 광고 이미지 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "광고 이미지 삭제", description = "특정 광고 이미지를 삭제합니다.")
    public ResponseEntity<JsonBody<Void>> deleteAdvertisementImage(@PathVariable Long id) {
        advertisementImageService.deleteAdvertisementImage(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(JsonBody.of(204, "광고 이미지 삭제 성공", null));
    }

    /**
     * 모든 광고 이미지 조회
     */
    @GetMapping
    @Operation(summary = "모든 광고 이미지 조회", description = "등록된 모든 광고 이미지를 조회합니다.")
    public ResponseEntity<JsonBody<List<AdvertisementImageDto>>> getAllAdvertisementImages() {
        List<AdvertisementImageDto> advertisementImages = advertisementImageService.getAllAdvertisementImages();
        return ResponseEntity.ok(JsonBody.of(200, "모든 광고 이미지 조회 성공", advertisementImages));
    }
}
