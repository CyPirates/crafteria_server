package com.example.crafteria_server.domain.equipment.service;

import com.example.crafteria_server.domain.equipment.dto.EquipmentDto;
import com.example.crafteria_server.domain.equipment.entity.Equipment;
import com.example.crafteria_server.domain.equipment.entity.EquipmentStatus;
import com.example.crafteria_server.domain.equipment.repository.EquipmentRepository;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
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
@Slf4j(topic = "EquipmentService")
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final FileService fileService;  // 파일 저장 관련 서비스

    // 장비 등록
    public EquipmentDto.EquipmentResponse createEquipment(EquipmentDto.EquipmentRequest request) {
        MultipartFile imageFile = request.getImage();
        File savedFile = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            savedFile = fileService.saveImage(imageFile);
        }

        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(EquipmentStatus.Available)  // 기본값으로 상태 설정
                .manufacturer(manufacturer)
                .image(savedFile)
                .printSpeed(request.getPrintSpeed())
                .build();

        log.info("[장비 등록] 이름: {}, 설명: {}, 제조사 ID: {}, 상태: {}, 출력속도: {}",
                equipment.getName(),
                equipment.getDescription(),
                manufacturer.getId(),
                equipment.getStatus(),
                equipment.getPrintSpeed()
        );

        Equipment savedEquipment = equipmentRepository.save(equipment);
        return EquipmentDto.EquipmentResponse.from(savedEquipment);
    }

    // 장비 조회
    public EquipmentDto.EquipmentResponse getEquipmentById(Long id) {
        Optional<Equipment> equipment = equipmentRepository.findById(id);
        return equipment.map(EquipmentDto.EquipmentResponse::from).orElse(null);
    }

    // 모든 장비 조회
    public List<EquipmentDto.EquipmentResponse> getAllEquipments() {
        List<Equipment> equipments = equipmentRepository.findAll();
        return equipments.stream()
                .map(EquipmentDto.EquipmentResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 제조사의 장비 조회
    public List<EquipmentDto.EquipmentResponse> getEquipmentsByManufacturer(Long manufacturerId) {
        // 제조사 유효성 확인
        Manufacturer manufacturer = manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 특정 제조사의 장비 목록 조회
        List<Equipment> equipments = equipmentRepository.findByManufacturerId(manufacturerId);
        return equipments.stream()
                .map(EquipmentDto.EquipmentResponse::from)
                .collect(Collectors.toList());
    }

    // 장비 수정
    public EquipmentDto.EquipmentResponse updateEquipment(Long id, EquipmentDto.EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (request.getPrintSpeed() != null) {
            equipment.setPrintSpeed(request.getPrintSpeed());
        }

        Manufacturer existingManufacturer = equipment.getManufacturer();

        // 입력된 제조사 ID와 장비의 기존 제조사 ID가 다른 경우 에러 처리
        if (!existingManufacturer.getId().equals(request.getManufacturerId())) {
            throw new IllegalArgumentException("The provided manufacturer ID does not match the equipment's current manufacturer.");
        }

        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 이미지 업데이트
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (equipment.getImage() != null) {
                fileService.deleteFile(equipment.getImage());
            }
            File updatedFile = fileService.saveImage(request.getImage());
            equipment.setImage(updatedFile);
        }

        // 기타 필드 업데이트
        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setManufacturer(manufacturer);

        Equipment updatedEquipment = equipmentRepository.save(equipment);
        return EquipmentDto.EquipmentResponse.from(updatedEquipment);
    }

    // 장비 삭제
    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        // 파일이 있는 경우 파일 삭제
        if (equipment.getImage() != null) {
            fileService.deleteFile(equipment.getImage());
        }

        // Equipment 엔티티 삭제
        equipmentRepository.delete(equipment);
    }

    // 장비 상태 업데이트
    public EquipmentDto.EquipmentResponse updateEquipmentStatus(Long id, EquipmentStatus status) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        equipment.setStatus(status);
        Equipment updatedEquipment = equipmentRepository.save(equipment);

        return EquipmentDto.EquipmentResponse.from(updatedEquipment);
    }
}

