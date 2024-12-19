package com.example.crafteria_server.domain.manufacturer.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.dto.ManufacturerDTO;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
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
    public ManufacturerDTO.ManufacturerResponse createManufacturer(
            ManufacturerDTO.ManufacturerRequest request,
            PrincipalDetails principalDetails) throws AccessDeniedException {
        // 현재 로그인된 유저 확인
        User dashboardUser = principalDetails.getUser();
        if (dashboardUser.getRole() != Role.DASHBOARD) {
            throw new AccessDeniedException("대시보드 권한이 없습니다.");
        }

        // MultipartFile을 File 엔티티로 변환하여 저장
        MultipartFile imageFile = request.getImage();
        File savedFile = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            savedFile = fileService.saveImage(imageFile); // 이미지 저장
        }

        // Manufacturer 엔티티 생성
        Manufacturer manufacturer = Manufacturer.builder()
                .name(request.getName())
                .introduction(request.getIntroduction())
                .address(request.getAddress())
                .dialNumber(request.getDialNumber())
                .representativeEquipment(request.getRepresentativeEquipment())
                .image(savedFile) // 저장된 이미지 파일
                .dashboardUser(dashboardUser) // 대시보드 유저 설정
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

    // 매칭 검증 메서드
    private Manufacturer validateManufacturerOwnership(Long manufacturerId, Long userId) throws AccessDeniedException {
        Manufacturer manufacturer = manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 요청 유저와 제조사의 대시보드 유저가 일치하지 않는 경우 예외 발생
        if (!manufacturer.getDashboardUser().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        return manufacturer;
    }

    // 제조사 수정
    public ManufacturerDTO.ManufacturerResponse updateManufacturer(
            Long id, ManufacturerDTO.ManufacturerRequest request, Long userId) throws AccessDeniedException {
        Manufacturer manufacturer = validateManufacturerOwnership(id, userId);

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

        Manufacturer updatedManufacturer = manufacturerRepository.save(manufacturer);
        return ManufacturerDTO.ManufacturerResponse.from(updatedManufacturer);
    }

    // 제조사 삭제
    public void deleteManufacturer(Long id, Long userId) throws AccessDeniedException {
        Manufacturer manufacturer = validateManufacturerOwnership(id, userId);

        // 파일 삭제
        if (manufacturer.getImage() != null) {
            fileService.deleteFile(manufacturer.getImage());
        }

        // 제조사 엔티티 삭제
        manufacturerRepository.delete(manufacturer);
    }
}