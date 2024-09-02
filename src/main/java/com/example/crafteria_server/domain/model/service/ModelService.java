package com.example.crafteria_server.domain.model.service;

import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j(topic = "MusicService")
@Transactional
@RequiredArgsConstructor
public class ModelService {
    private final ModelRepository modelRepository;
    private final FileService fileService;

    public List<UserModelDto.Response> getPopularList(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return modelRepository.findAll(pageable).stream()
                .map(UserModelDto.Response::from)
                .toList();
    }

    public UserModelDto.Response getModelDetail(Long modelId) {
        return UserModelDto.Response.from(modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "모델을 찾을 수 없습니다.")));
    }

//    public UserModelDto.Response uploadModel(UserModelDto.UploadRequest request) {
//
//
//    }

}
