package com.example.crafteria_server.domain.search.service;

import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.search.dto.SearchDto;
import com.example.crafteria_server.domain.user.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {
    private final ManufacturerRepository manufacturerRepository;
    private final AuthorRepository authorRepository;
    private final ModelRepository modelRepository;

    // 제조사 검색 메서드
    public List<SearchDto.SearchResultDto> searchManufacturersByName(String name) {
        return manufacturerRepository.findByNameContaining(name)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchManufacturersByDescription(String description) {
        return manufacturerRepository.findByIntroductionContaining(description)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchManufacturersByNameAndDescription(String keyword) {
        return manufacturerRepository.findByNameContainingOrIntroductionContaining(keyword, keyword)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    // 작가 검색 메서드
    public List<SearchDto.SearchResultDto> searchAuthorsByName(String name) {
        return authorRepository.findByRealnameContaining(name)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchAuthorsByDescription(String description) {
        return authorRepository.findByIntroductionContaining(description)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchAuthorsByNameAndDescription(String keyword) {
        return authorRepository.findByRealnameContainingOrIntroductionContaining(keyword, keyword)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
    }

    // 모델 검색 메서드
    public List<SearchDto.SearchResultDto> searchModelsByName(String name) {
        return modelRepository.findByNameContaining(name)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchModelsByDescription(String description) {
        return modelRepository.findByDescriptionContaining(description)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
    }

    public List<SearchDto.SearchResultDto> searchModelsByNameAndDescription(String keyword) {
        return modelRepository.findByNameContainingOrDescriptionContaining(keyword, keyword)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
    }
}
