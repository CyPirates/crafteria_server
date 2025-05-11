package com.example.crafteria_server.domain.search.service;

import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.search.dto.SearchDto;
import com.example.crafteria_server.domain.user.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "SearchService")
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final ManufacturerRepository manufacturerRepository;
    private final AuthorRepository authorRepository;
    private final ModelRepository modelRepository;

    // 제조사 검색
    public List<SearchDto.SearchResultDto> searchManufacturersByName(String name) {
        List<SearchDto.SearchResultDto> result = manufacturerRepository.findByNameContaining(name)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[제조사 이름 검색] keyword: {}, 결과 수: {}", name, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchManufacturersByDescription(String description) {
        List<SearchDto.SearchResultDto> result = manufacturerRepository.findByIntroductionContaining(description)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[제조사 설명 검색] keyword: {}, 결과 수: {}", description, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchManufacturersByNameAndDescription(String keyword) {
        List<SearchDto.SearchResultDto> result = manufacturerRepository.findByNameContainingOrIntroductionContaining(keyword, keyword)
                .stream()
                .map(manufacturer -> new SearchDto.SearchResultDto("Manufacturer", manufacturer.getId(), manufacturer.getName(), manufacturer.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[제조사 이름+설명 검색] keyword: {}, 결과 수: {}", keyword, result.size());
        return result;
    }

    // 작가 검색
    public List<SearchDto.SearchResultDto> searchAuthorsByName(String name) {
        List<SearchDto.SearchResultDto> result = authorRepository.findByRealnameContaining(name)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[작가 이름 검색] keyword: {}, 결과 수: {}", name, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchAuthorsByDescription(String description) {
        List<SearchDto.SearchResultDto> result = authorRepository.findByIntroductionContaining(description)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[작가 설명 검색] keyword: {}, 결과 수: {}", description, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchAuthorsByNameAndDescription(String keyword) {
        List<SearchDto.SearchResultDto> result = authorRepository.findByRealnameContainingOrIntroductionContaining(keyword, keyword)
                .stream()
                .map(author -> new SearchDto.SearchResultDto("Author", author.getId(), author.getRealname(), author.getIntroduction(), null))
                .collect(Collectors.toList());
        log.info("[작가 이름+설명 검색] keyword: {}, 결과 수: {}", keyword, result.size());
        return result;
    }

    // 모델 검색
    public List<SearchDto.SearchResultDto> searchModelsByName(String name) {
        List<SearchDto.SearchResultDto> result = modelRepository.findByNameContaining(name)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
        log.info("[모델 이름 검색] keyword: {}, 결과 수: {}", name, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchModelsByDescription(String description) {
        List<SearchDto.SearchResultDto> result = modelRepository.findByDescriptionContaining(description)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
        log.info("[모델 설명 검색] keyword: {}, 결과 수: {}", description, result.size());
        return result;
    }

    public List<SearchDto.SearchResultDto> searchModelsByNameAndDescription(String keyword) {
        List<SearchDto.SearchResultDto> result = modelRepository.findByNameContainingOrDescriptionContaining(keyword, keyword)
                .stream()
                .map(model -> new SearchDto.SearchResultDto("Model", model.getId(), model.getName(), model.getDescription(), null))
                .collect(Collectors.toList());
        log.info("[모델 이름+설명 검색] keyword: {}, 결과 수: {}", keyword, result.size());
        return result;
    }
}
