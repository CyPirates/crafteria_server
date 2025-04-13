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
import java.util.UUID;
import java.util.stream.Collectors;

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
        List<Model> models = modelRepository.findAllByIsDeletedFalseOrderByViewCountDesc(pageable).getContent();

        return models.stream()
                .map(model -> {
                    boolean purchaseAvailability = userId
                            .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, model.getId()))
                            .orElse(true);
                    return UserModelDto.ModelResponse.from(model, purchaseAvailability);
                })
                .collect(Collectors.toList());
    }

    public UserModelDto.ModelResponse getModelDetail(Long modelId, Optional<Long> userId) {
        Model model = modelRepository.findByIdAndIsDeletedFalse(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        boolean purchaseAvailability = userId
                .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, modelId))
                .orElse(true);

        model.setViewCount(model.getViewCount() + 1);
        modelRepository.save(model);

        return UserModelDto.ModelResponse.from(model, purchaseAvailability);
    }

    public UserModelDto.ModelResponse uploadModel(Long userId, UserModelDto.ModelUploadRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        log.info("userid: {}", user.getId());

        Author author = authorRepository.findById(user.getId()).orElseGet(() -> {
            return Author.builder()
                    .user(user)
                    .id(user.getId())
                    .realname(user.getRealname())  // User의 realname을 Author의 realname에 복사
                    .rating(5)
                    .modelCount(0)
                    .viewCount(0)
                    .build();
        });

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
                .category(request.getCategory())
                .modelFile(modelFile)
                .build();

        modelRepository.save(newModel);
        return UserModelDto.ModelResponse.from(newModel, false);  // 업로드한 도면은 구매 불가능
    }

    public List<UserModelDto.ModelResponse> getMyDownloadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<ModelPurchase> purchases = modelPurchaseRepository.findAllByUserIdOrderByCreateDateDesc(userId, pageable).getContent();

        return purchases.stream()
                .map(purchase -> UserModelDto.ModelResponse.from(purchase.getModel(), false))
                .collect(Collectors.toList());
    }

    public UserModelDto.ModelResponse purchaseModel(Long userId, Long modelId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "모델을 찾을 수 없습니다."));

        if (model.getAuthor().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자신이 판매중인 도면은 구매할 수 없습니다.");
        }

        modelPurchaseRepository.findByUserIdAndModelId(userId, modelId).ifPresent(modelPurchase -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 구매한 모델입니다.");
        });

        String paymentId = UUID.randomUUID().toString(); // 🔥 결제 ID 생성

        ModelPurchase savedPurchase = modelPurchaseRepository.save(
                ModelPurchase.builder()
                        .user(user)
                        .model(model)
                        .paymentId(paymentId)
                        .build()
        );

        model.setDownloadCount(model.getDownloadCount() + 1);
        modelRepository.save(model);

        return UserModelDto.ModelResponse.from(savedPurchase); // 🔥 구매 내역 기준으로 응답
    }

    public List<UserModelDto.ModelResponse> getMyUploadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository.findAllByAuthorIdAndIsDeletedFalseOrderByCreateDateDesc(userId, pageable).getContent();

        return models.stream()
                .map(model -> UserModelDto.ModelResponse.from(model, false))
                .collect(Collectors.toList());
    }

    private boolean checkIfModelPurchased(Long userId, Long modelId) {
        return modelPurchaseRepository.findByUserIdAndModelId(userId, modelId).isPresent();
    }

    // 도면 수정
    public UserModelDto.ModelResponse updateModel(Long modelId, Long userId, UserModelDto.ModelUploadRequest request) {
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 수정할 권한이 없습니다.");
        }

        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setPrice(request.getPrice());
        model.setWidthSize(request.getWidthSize());
        model.setLengthSize(request.getLengthSize());
        model.setHeightSize(request.getHeightSize());
        model.setCategory(request.getCategory());

        if (request.getModelFile() != null) {
            File modelFile = fileService.saveModel(request.getModelFile());
            model.setModelFile(modelFile);
        }

        modelRepository.save(model);
        return UserModelDto.ModelResponse.from(model, false);  // 업데이트한 도면은 구매 불가능
    }

    // 도면 삭제
    public void deleteModel(Long modelId, Long userId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 삭제할 권한이 없습니다.");
        }

        model.setDeleted(true);
        modelRepository.save(model);
    }
}

