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
import com.example.crafteria_server.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final UserService userService;

    public List<UserModelDto.ModelResponse> getPopularList(int page, Optional<Long> userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository.findAllByIsDeletedFalseOrderByViewCountDesc(pageable).getContent();

        return models.stream()
                .map(model -> {
                    boolean purchaseAvailability = userId
                            .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, model.getId()))
                            .orElse(true);
                    return UserModelDto.ModelResponse.from(model, purchaseAvailability, model.isDownloadable());
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

        return UserModelDto.ModelResponse.from(model, purchaseAvailability, model.isDownloadable());
    }

    public UserModelDto.ModelResponse uploadModel(Long userId, UserModelDto.ModelUploadRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Author author = authorRepository.findById(user.getId()).orElseGet(() -> Author.builder()
                .user(user)
                .id(user.getId())
                .realname(user.getRealname())
                .rating(5)
                .modelCount(0)
                .viewCount(0)
                .build());

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
                .isDownloadable(request.isDownloadable())
                .build();

        modelRepository.save(newModel);

        // 업로드 수 증가 및 판매자 레벨 갱신
        user.setTotalUploadCount(user.getTotalUploadCount() + 1);
        userService.updateUserLevel(user);
        userRepository.save(user);

        log.info("[도면 업로드 처리] 유저ID: {}, 업로드 후 총 업로드 수: {}", userId, user.getTotalUploadCount());

        log.info("[도면 업로드] userId={}, modelName='{}', price={}, downloadable={}, category={}, fileName={}",
                userId,
                request.getName(),
                request.getPrice(),
                request.isDownloadable(),
                request.getCategory(),
                request.getModelFile().getOriginalFilename()
        );

        return UserModelDto.ModelResponse.from(newModel, false, newModel.isDownloadable());
    }

    public List<UserModelDto.ModelResponse> getMyDownloadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ModelPurchase> purchases = modelPurchaseRepository
                .findAllByUserIdAndVerifiedTrueOrderByCreateDateDesc(userId, pageable);

        return purchases.stream()
                .map(purchase -> {
                    Model model = purchase.getModel();
                    boolean downloadable = model.isDownloadable();
                    return UserModelDto.ModelResponse.from(model, false, downloadable);
                })
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

        modelPurchaseRepository.findByUserIdAndModelIdAndVerifiedTrue(userId, modelId).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 구매한 모델입니다.");
        });

        ModelPurchase purchase = ModelPurchase.builder()
                .user(user)
                .model(model)
                .paymentId(model.getPrice() > 0 ? UUID.randomUUID().toString() : null)
                .verified(model.getPrice() == 0)
                .build();

        ModelPurchase savedPurchase = modelPurchaseRepository.save(purchase);

        model.setDownloadCount(model.getDownloadCount() + 1);
        modelRepository.save(model);



        // 구매자 유저 레벨 업데이트
        user.setTotalPurchaseCount(user.getTotalPurchaseCount() + 1);
        user.setTotalPurchaseAmount(user.getTotalPurchaseAmount() + model.getPrice());
        userService.updateUserLevel(user);
        userRepository.save(user);

        // 판매자 레벨 업데이트
        User seller = model.getAuthor().getUser();
        seller.setTotalSalesCount(seller.getTotalSalesCount() + 1);
        seller.setTotalSalesAmount(seller.getTotalSalesAmount() + model.getPrice());
        userService.updateUserLevel(seller);
        userRepository.save(seller);

        log.info("[도면 구매 처리] 구매자: {}, 판매자: {}, 도면ID: {}, 가격: {}, 다운로드 가능: {}",
                user.getUsername(), seller.getUsername(), modelId, model.getPrice(), model.isDownloadable());

        return UserModelDto.ModelResponse.from(savedPurchase, model.isDownloadable());
    }

    public List<UserModelDto.ModelResponse> getMyUploadedModelList(int page, Long userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository.findAllByAuthorIdAndIsDeletedFalseOrderByCreateDateDesc(userId, pageable).getContent();

        return models.stream()
                .map(model -> UserModelDto.ModelResponse.from(model, false, model.isDownloadable()))
                .collect(Collectors.toList());
    }

    private boolean checkIfModelPurchased(Long userId, Long modelId) {
        return modelPurchaseRepository.findByUserIdAndModelIdAndVerifiedTrue(userId, modelId).isPresent();
    }

    public UserModelDto.ModelResponse updateModel(Long modelId, Long userId, UserModelDto.ModelUploadRequest request) {
        Model model = modelRepository.findById(modelId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 수정할 권한이 없습니다.");
        }

        log.info("[도면 수정 - 수정 전] modelId={}, name={}, price={}, category={}, downloadable={}",
                model.getId(), model.getName(), model.getPrice(), model.getCategory(), model.isDownloadable());

        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setPrice(request.getPrice());
        model.setWidthSize(request.getWidthSize());
        model.setLengthSize(request.getLengthSize());
        model.setHeightSize(request.getHeightSize());
        model.setCategory(request.getCategory());
        model.setDownloadable(request.isDownloadable());

        if (request.getModelFile() != null) {
            File modelFile = fileService.saveModel(request.getModelFile());
            model.setModelFile(modelFile);
        }

        log.info("[도면 수정 - 수정 후] modelId={}, name={}, price={}, category={}, downloadable={}",
                model.getId(), model.getName(), model.getPrice(), model.getCategory(), model.isDownloadable());

        modelRepository.save(model);
        return UserModelDto.ModelResponse.from(model, false, model.isDownloadable());
    }

    public void deleteModel(Long modelId, Long userId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        if (!model.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 도면을 삭제할 권한이 없습니다.");
        }

        log.info("[도면 삭제] modelId={}, modelName={}, deletedByUserId={}",
                model.getId(), model.getName(), userId);

        model.setDeleted(true);
        modelRepository.save(model);
    }
}

