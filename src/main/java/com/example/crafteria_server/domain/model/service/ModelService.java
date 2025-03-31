package com.example.crafteria_server.domain.model.service;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
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
import java.util.Optional;

@Service
@Slf4j(topic = "ModelService")
@Transactional
@RequiredArgsConstructor
public class ModelService {
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final ModelPurchaseRepository modelPurchaseRepository;
    private final FileService fileService;

    public List<UserModelDto.ModelResponse> getPopularList(int page, Optional<Long> userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository.findAllOrderByViewCountDesc(pageable).getContent();

        return models.stream()
                .map(model -> {
                    UserModelDto.ModelResponse response = UserModelDto.ModelResponse.from(model);
                    userId.ifPresent(uId -> {
                        response.setPurchased(checkIfModelPurchased(uId, model.getId()));
                    });
                    return response;
                })
                .toList();
    }

    public UserModelDto.ModelResponse getModelDetail(Long modelId, Long userId) {
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));
        boolean isPurchased = checkIfModelPurchased(userId, modelId);

        UserModelDto.ModelResponse response = UserModelDto.ModelResponse.from(model);
        response.setPurchased(isPurchased);  // 구매 여부 설정

        // 조회수 업데이트
        model.setViewCount(model.getViewCount() + 1);
        modelRepository.save(model);

        return response;
    }

    public UserModelDto.ModelResponse uploadModel(Long userId, UserModelDto.ModelUploadRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        log.info("userid: {}", user.getId());

        // Author 엔티티를 가져오거나 없으면 생성
        Author author = authorRepository.findById(user.getId()).orElseGet(() -> {
            Author newAuthor = Author.builder()
                    .user(user)
                    .id(user.getId())
                    .realname(user.getRealname())  // User의 realname을 Author의 realname에 복사
                    .rating(5)
                    .modelCount(0)
                    .viewCount(0)
                    .build();
            return newAuthor;
        });

        // Author가 새로 생성되었거나 기존 Author의 realname이 없을 경우 업데이트
        if (author.getRealname() == null) {
            author.setRealname(user.getRealname());
        }

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
                .widthSize(request.getWidthSize())
                .lengthSize(request.getLengthSize())
                .heightSize(request.getHeightSize())
                .modelFile(modelFile)
                .build();

        return UserModelDto.ModelResponse.from(modelRepository.save(newModel));
    }

    public List<UserModelDto.ModelResponse> getMyDownloadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        return modelPurchaseRepository.findAllByUserIdOrderByCreateDateDesc(userId, pageable).stream()
                .map(UserModelDto.ModelResponse::from)
                .toList();
    }

    public UserModelDto.ModelResponse purchaseModel(Long userId, Long modelId) {
        // 유저 조회
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 모델 조회
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "모델을 찾을 수 없습니다."));

        // 자신이 올린 도면인지 확인
        if (model.getAuthor().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자신이 판매중인 도면은 구매할 수 없습니다.");
        }

        // 이미 구매한 도면인지 확인
        modelPurchaseRepository.findByUserIdAndModelId(userId, modelId).ifPresent(modelPurchase -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 구매한 모델입니다.");
        });

        // 구매 정보 저장
        modelPurchaseRepository.save(ModelPurchase.builder()
                .user(user)
                .model(model)
                .build());

        // 다운로드 수 증가
        model.setDownloadCount(model.getDownloadCount() + 1);

        // 응답 DTO 반환
        return UserModelDto.ModelResponse.from(model);
    }

    public List<UserModelDto.ModelResponse> getMyUploadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        return modelRepository.findAllByAuthorIdOrderByCreateDateDesc(userId, pageable).stream()
                .map(UserModelDto.ModelResponse::from)
                .toList();
    }

    private boolean checkIfModelPurchased(Long userId, Long modelId) {
        return modelPurchaseRepository.findByUserIdAndModelId(userId, modelId).isPresent();
    }

    // 도면 수정
    public UserModelDto.ModelResponse updateModel(Long modelId, Long userId, UserModelDto.ModelUploadRequest request) {
        Model model = modelRepository.findById(modelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        // 유저가 도면의 작가인지 확인
        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 수정할 권한이 없습니다.");
        }

        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setPrice(request.getPrice());
        model.setWidthSize(request.getWidthSize());
        model.setLengthSize(request.getLengthSize());
        model.setHeightSize(request.getHeightSize());

        // 파일 업데이트 (선택적)
        if (request.getModelFile() != null) {
            File modelFile = fileService.saveModel(request.getModelFile());
            model.setModelFile(modelFile);
        }

        return UserModelDto.ModelResponse.from(modelRepository.save(model));
    }

    // 도면 삭제
    public void deleteModel(Long modelId, Long userId) {
        Model model = modelRepository.findById(modelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        // 유저가 도면의 작가인지 확인
        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 삭제할 권한이 없습니다.");
        }

        modelRepository.delete(model);
    }
}

