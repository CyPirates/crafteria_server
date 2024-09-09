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
        // MultipartFile을 File 엔티티로 변환하여 저장
        MultipartFile imageFile = request.getImage();
        File savedFile = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            savedFile = fileService.saveImage(imageFile);  // 이미지 저장
        }

        // Manufacturer 엔티티 조회
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // Equipment 엔티티 생성
        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(EquipmentStatus.valueOf(request.getStatus()))  // Enum 변환
                .manufacturer(manufacturer)
                .image(savedFile)  // 저장된 이미지 파일을 Equipment에 설정
                .build();

        // Equipment 엔티티 저장
        Equipment savedEquipment = equipmentRepository.save(equipment);

        // 응답 DTO로 변환하여 반환
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

    // 장비 업데이트
    public EquipmentDto.EquipmentResponse updateEquipment(Long id, EquipmentDto.EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        // Manufacturer 조회
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

        // 기존 이미지 파일이 있으면 삭제하고 새로운 파일로 대체
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
        equipment.setStatus(EquipmentStatus.valueOf(request.getStatus()));  // Enum 변환
        equipment.setManufacturer(manufacturer);

        // Equipment 엔티티 저장
        Equipment updatedEquipment = equipmentRepository.save(equipment);

        // 응답 DTO로 변환하여 반환
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
}

