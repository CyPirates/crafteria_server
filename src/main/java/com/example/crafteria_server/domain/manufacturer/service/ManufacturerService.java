package com.example.crafteria_server.domain.manufacturer.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.dto.ManufacturerDTO;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ManufacturerService")
@RequiredArgsConstructor
@Transactional
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final FileService fileService;  // 파일 저장 관련 서비스

    // 제조사 등록
    public ManufacturerDTO.ManufacturerResponse createManufacturer(ManufacturerDTO.ManufacturerRequest request) {
        // MultipartFile을 File 엔티티로 변환하여 저장
        MultipartFile imageFile = request.getImage();
        File savedFile = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            savedFile = fileService.saveImage(imageFile);  // 이미지 저장
        }

        // Manufacturer 엔티티 생성
        Manufacturer manufacturer = Manufacturer.builder()
                .name(request.getName())
                .introduction(request.getIntroduction())
                .address(request.getAddress())
                .dialNumber(request.getDialNumber())
                .representativeEquipment(request.getRepresentativeEquipment())
                .image(savedFile)  // 저장된 이미지 파일을 Manufacturer에 설정
                .build();

        // Manufacturer 엔티티 저장
        Manufacturer savedManufacturer = manufacturerRepository.save(manufacturer);

        // 응답 DTO로 변환하여 반환
        return ManufacturerDTO.ManufacturerResponse.from(savedManufacturer);
    }

    // 제조사 조회
    public ManufacturerDTO.ManufacturerResponse getManufacturerById(Long id) {
        Optional<Manufacturer> optionalManufacturer = manufacturerRepository.findById(id);

        if (optionalManufacturer.isEmpty()) {
            return null;
        }

        Manufacturer manufacturer = optionalManufacturer.get();

        // LazyInitializationException 방지: equipmentList 초기화
        manufacturer.getEquipmentList().size();

        return ManufacturerDTO.ManufacturerResponse.from(manufacturer);
    }

    // 모든 제조사 조회
    public List<ManufacturerDTO.ManufacturerResponse> getAllManufacturers() {
        List<Manufacturer> manufacturers = manufacturerRepository.findAll();

        // 각 제조사의 equipmentList 초기화
        for (Manufacturer manufacturer : manufacturers) {
            manufacturer.getEquipmentList().size();
        }

        return manufacturers.stream()
                .map(ManufacturerDTO.ManufacturerResponse::from)
                .collect(Collectors.toList());
    }

    // 제조사 업데이트
    public ManufacturerDTO.ManufacturerResponse updateManufacturer(Long id, ManufacturerDTO.ManufacturerRequest request) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 기존 이미지 파일이 있으면 삭제하고 새로운 파일로 대체
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (manufacturer.getImage() != null) {
                fileService.deleteFile(manufacturer.getImage());
            }
            File updatedFile = fileService.saveImage(request.getImage());
            manufacturer.setImage(updatedFile);
        }

        // 기타 필드 업데이트
        manufacturer.setName(request.getName());
        manufacturer.setIntroduction(request.getIntroduction());
        manufacturer.setAddress(request.getAddress());
        manufacturer.setDialNumber(request.getDialNumber());
        manufacturer.setRepresentativeEquipment(request.getRepresentativeEquipment());

        // Manufacturer 엔티티 저장
        Manufacturer updatedManufacturer = manufacturerRepository.save(manufacturer);

        // equipmentList 초기화
        updatedManufacturer.getEquipmentList().size();

        // 응답 DTO로 변환하여 반환
        return ManufacturerDTO.ManufacturerResponse.from(updatedManufacturer);
    }

    // 제조사 삭제
    public void deleteManufacturer(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 파일이 있는 경우 파일 삭제
        if (manufacturer.getImage() != null) {
            fileService.deleteFile(manufacturer.getImage());
        }

        // Manufacturer 엔티티 삭제
        manufacturerRepository.delete(manufacturer);
    }
}