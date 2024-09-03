package com.example.crafteria_server.domain.model.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.user.entity.Author;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.AuthorRepository;
import com.example.crafteria_server.domain.user.repository.UserRepository;
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
@Slf4j(topic = "ModelService")
@Transactional
@RequiredArgsConstructor
public class ModelService {
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final FileService fileService;

    public List<UserModelDto.Response> getPopularList(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return modelRepository.findAllOrderByViewCountDesc(pageable).stream()
                .map(UserModelDto.Response::from)
                .toList();
    }

    public UserModelDto.Response getModelDetail(Long modelId) {
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));
        model.setViewCount(model.getViewCount() + 1);
        return UserModelDto.Response.from(modelRepository.save(model));
    }

    public UserModelDto.Response uploadModel(Long userId, UserModelDto.UploadRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        log.info("userid: {}", user.getId());
        Author author = authorRepository.findById(user.getId()).orElse(Author.builder()
                .user(user)
                .id(user.getId())
                .rating(5)
                .modelCount(0)
                .viewCount(0)
                .build());
        authorRepository.save(author);

        File modelFile = fileService.saveModel(request.getModelFile());

        Model newModel = Model.builder()
                .author(author)
                .name(request.getName())
                .description(request.getDescription())
                .rating(0)
                .price(request.getPrice())
                .viewCount(0)
                .downloadCount(0)
                .minimumSize(request.getMinimumSize())
                .maximumSize(request.getMaximumSize())
                .modelFile(modelFile)
                .build();

        return UserModelDto.Response.from(modelRepository.save(newModel));
    }

}
