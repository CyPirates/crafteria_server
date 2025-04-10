package com.example.crafteria_server.domain.technology.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.technology.dto.TechnologyDto;
import com.example.crafteria_server.domain.technology.entity.Technology;
import com.example.crafteria_server.domain.technology.repository.TechnologyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TechnologyService {
    private final TechnologyRepository technologyRepository;
    private final FileService fileService;
    private final ManufacturerRepository manufacturerRepository;

    public Technology createTechnology(TechnologyDto.TechnologyRequest request, FileService fileService, ManufacturerRepository manufacturerRepository) throws IOException {
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid manufacturer ID"));

        File image = null;
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            image = fileService.saveImage(request.getImageFile());
        }

        Technology technology = Technology.builder()
                .manufacturer(manufacturer)
                .material(request.getMaterial())
                .description(request.getDescription())
                .colorValue(request.getColorValue())
                .image(image)
                .pricePerHour(request.getPricePerHour())
                .build();

        return technologyRepository.save(technology);
    }

    public Technology updateTechnology(Long technologyId, TechnologyDto.TechnologyRequest request, FileService fileService) throws IOException {
        Technology technology = technologyRepository.findById(technologyId)
                .orElseThrow(() -> new EntityNotFoundException("Technology not found"));

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            File newImage = fileService.saveImage(request.getImageFile());
            technology.setImage(newImage);
        }

        technology.setMaterial(request.getMaterial());
        technology.setDescription(request.getDescription());
        technology.setColorValue(request.getColorValue());
        technology.setPricePerHour(request.getPricePerHour());

        return technologyRepository.save(technology);
    }

    public void deleteTechnology(Long technologyId) {
        Technology technology = technologyRepository.findById(technologyId)
                .orElseThrow(() -> new EntityNotFoundException("Technology not found"));
        if (technology.getImage() != null) {
            fileService.deleteFile(technology.getImage());
        }
        technologyRepository.delete(technology);
    }

    public List<Technology> getAllTechnologiesByManufacturer(Long manufacturerId) {
        return technologyRepository.findByManufacturerId(manufacturerId);
    }

    public TechnologyDto.TechnologyResponse getTechnologyById(Long id) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Technology not found with id: " + id));

        return TechnologyDto.TechnologyResponse.from(technology);
    }
}
