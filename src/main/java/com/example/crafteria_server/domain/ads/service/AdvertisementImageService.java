package com.example.crafteria_server.domain.ads.service;

import com.example.crafteria_server.domain.ads.dto.AdvertisementImageDto;
import com.example.crafteria_server.domain.ads.entity.AdvertisementImage;
import com.example.crafteria_server.domain.ads.repository.AdvertisementImageRepository;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "AdvertisementImageService")
public class AdvertisementImageService {

    private final AdvertisementImageRepository advertisementImageRepository;
    private final FileService fileService;

    /**
     * 광고 이미지 업로드
     */
    public AdvertisementImage uploadAdvertisementImage(MultipartFile imageFile, String title, String description) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 필요합니다.");
        }

        // 이미지 파일 저장
        File savedFile = fileService.saveImage(imageFile);

        // 광고 이미지 엔터티 생성
        AdvertisementImage advertisementImage = AdvertisementImage.builder()
                .file(savedFile)
                .title(title)
                .description(description)
                .build();

        // DB 저장
        return advertisementImageRepository.save(advertisementImage);
    }

    /**
     * 광고 이미지 삭제
     */
    @Transactional
    public void deleteAdvertisementImage(Long advertisementImageId) {
        AdvertisementImage advertisementImage = advertisementImageRepository.findById(advertisementImageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 광고 이미지를 찾을 수 없습니다."));

        // 연관된 파일 삭제 (DB에서 제거)
        if (advertisementImage.getFile() != null) {
            File file = advertisementImage.getFile();
            advertisementImage.setFile(null); // 연관 관계 끊기
            advertisementImageRepository.save(advertisementImage); // 변경 사항 영속화
            fileService.deleteFile(file); // 파일 삭제
        }

        // 광고 이미지 삭제
        advertisementImageRepository.delete(advertisementImage);
    }

    /**
     * 모든 광고 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<AdvertisementImageDto> getAllAdvertisementImages() {
        return advertisementImageRepository.findAll().stream()
                .map(AdvertisementImageDto::from) // DTO로 변환
                .toList();
    }
}
