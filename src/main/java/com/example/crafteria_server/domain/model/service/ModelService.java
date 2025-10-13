package com.example.crafteria_server.domain.model.service;

import com.example.crafteria_server.domain.coupon.entity.Coupon;
import com.example.crafteria_server.domain.coupon.entity.CouponType;
import com.example.crafteria_server.domain.coupon.repository.CouponRepository;
import com.example.crafteria_server.domain.coupon.service.CouponService;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.model.dto.ModelPurchaseRequest;
import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.entity.ModelAsset;
import com.example.crafteria_server.domain.model.entity.ModelDescriptionImage;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
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
    private final CouponRepository couponRepository;
    private final FileService fileService;
    private final UserService userService;
    private final CouponService couponService;


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
        if (author.getRealname() == null) author.setRealname(user.getRealname());
        authorRepository.save(author);

        // ✅ 업로드에서 STL은 필수
        MultipartFile[] stlArr = request.getModelFiles();
        if (stlArr == null || stlArr.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "STL 파일은 1개 이상 필수입니다.");
        }
        if (stlArr.length > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "STL은 최대 100개까지 업로드 가능합니다.");
        }

        MultipartFile[] descArr = Optional.ofNullable(request.getDescriptionImages()).orElse(new MultipartFile[0]);
        if (descArr.length > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "설명 이미지는 최대 10개까지 업로드 가능합니다.");
        }

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
                .isDownloadable(request.isDownloadable())
                .assets(new ArrayList<>())
                .descriptionImages(new ArrayList<>())
                .build();

        // 설명 이미지 저장
        int dIdx = 0;
        for (MultipartFile img : descArr) {
            if (img == null || img.isEmpty()) continue;
            File saved = fileService.saveImage(img);
            ModelDescriptionImage di = ModelDescriptionImage.builder()
                    .model(newModel)
                    .file(saved)
                    .sortOrder(dIdx++)
                    .build();
            newModel.getDescriptionImages().add(di);
        }

        // STL 저장
        int sIdx = 0;
        for (MultipartFile stl : stlArr) {
            if (stl == null || stl.isEmpty()) continue;
            requireStlOrThrow(stl);
            File saved = fileService.saveModel(stl);
            ModelAsset asset = ModelAsset.builder()
                    .model(newModel)
                    .file(saved)
                    .sortOrder(sIdx++)
                    .build();
            newModel.getAssets().add(asset);
        }

        // 대표 STL 지정(없으면 첫 번째)
        if (newModel.getPrimaryAsset() == null && !newModel.getAssets().isEmpty()) {
            newModel.setPrimaryAsset(newModel.getAssets().get(0));
        }

        modelRepository.save(newModel);

        // 통계
        user.setTotalUploadCount(user.getTotalUploadCount() + 1);
        userService.updateUserLevel(user);
        userRepository.save(user);

        log.info("[도면 업로드] userId={}, name='{}', price={}, downloadable={}, stlCount={}, descImgCount={}",
                userId, request.getName(), request.getPrice(), request.isDownloadable(),
                newModel.getAssets().size(), newModel.getDescriptionImages().size());

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

        // 메타 업데이트
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setPrice(request.getPrice());
        model.setWidthSize(request.getWidthSize());
        model.setLengthSize(request.getLengthSize());
        model.setHeightSize(request.getHeightSize());
        model.setCategory(request.getCategory());
        model.setDownloadable(request.isDownloadable());

        // 설명 이미지: null이면 유지 / 값 오면 교체(빈배열=전체 제거)
        MultipartFile[] descArr = request.getDescriptionImages(); // null 허용
        if (descArr != null) {
            // 기존 설명 이미지 엔티티/파일 정리 (파일 삭제는 정책에 맞게)
            if (model.getDescriptionImages() == null) model.setDescriptionImages(new ArrayList<>());
            for (ModelDescriptionImage di : model.getDescriptionImages()) {
                // 필요 시 GCS 삭제:
                // fileService.deleteFile(di.getFile());
            }
            model.getDescriptionImages().clear();

            if (descArr.length > 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "설명 이미지는 최대 10개까지 업로드 가능합니다.");
            }
            int dIdx = 0;
            for (MultipartFile img : descArr) {
                if (img == null || img.isEmpty()) continue;
                File saved = fileService.saveImage(img);
                ModelDescriptionImage nd = ModelDescriptionImage.builder()
                        .model(model)
                        .file(saved)
                        .sortOrder(dIdx++)
                        .build();
                model.getDescriptionImages().add(nd);
            }
        }

        // STL: null이면 유지 / 값 오면 교체(빈배열인 경우 → 교체 요청으로 간주할지 무시할지 정책 선택)
        MultipartFile[] stlArr = request.getModelFiles(); // 수정에서는 null 허용(유지)
        if (stlArr != null) {
            if (stlArr.length > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "STL은 최대 100개까지 업로드 가능합니다.");
            }

            // 기존 STL 자산 정리 (파일 삭제는 정책에 맞게)
            if (model.getAssets() == null) model.setAssets(new ArrayList<>());
            for (ModelAsset asset : model.getAssets()) {
                // 필요 시 GCS 삭제:
                // fileService.deleteFile(asset.getFile());
            }
            model.getAssets().clear();

            int sIdx = 0;
            for (MultipartFile stl : stlArr) {
                if (stl == null || stl.isEmpty()) continue;
                requireStlOrThrow(stl);
                File saved = fileService.saveModel(stl);
                ModelAsset na = ModelAsset.builder()
                        .model(model)
                        .file(saved)
                        .sortOrder(sIdx++)
                        .build();
                model.getAssets().add(na);
            }
            // 대표 STL 재지정
            if (!model.getAssets().isEmpty()) {
                model.setPrimaryAsset(model.getAssets().get(0));
            } else {
                // 비어있게 두고 싶지 않다면 에러로 처리할 수도 있음.
                // 여기서는 "교체를 요청했는데 0개 파일"이면 기존을 지운 셈이니, 정책에 맞게 막을 수도 있음.
                model.setPrimaryAsset(null);
            }
        }

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

    public UserModelDto.ModelResponse purchaseModelWithCoupon(Long userId, ModelPurchaseRequest request) {
        log.info("[도면 구매 요청 시작] userId={}, modelId={}, couponId={}", userId, request.getModelId(), request.getCouponId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모델을 찾을 수 없습니다."));

        if (model.getAuthor().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자신이 등록한 도면은 구매할 수 없습니다.");
        }

        modelPurchaseRepository.findByUserIdAndModelIdAndVerifiedTrue(user.getId(), model.getId())
                .ifPresent(p -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 구매한 모델입니다.");
                });

        int originalPrice = (int) model.getPrice();
        int discount = 0;
        Coupon appliedCoupon = null;

        if (request.getCouponId() != null) {
            appliedCoupon = couponService.validateModelCoupon(request.getCouponId(), userId);
            discount = (originalPrice * appliedCoupon.getDiscountRate()) / 100;
            discount = Math.min(discount, appliedCoupon.getMaxDiscountAmount());

            log.info("[쿠폰 적용] couponId={}, 할인율={}%, 할인금액={}", appliedCoupon.getId(), appliedCoupon.getDiscountRate(), discount);
        } else {
            log.info("[쿠폰 미적용] 원가만 결제 진행");
        }

        int discountedPrice = originalPrice - discount;
        int vat = (int) Math.ceil(discountedPrice * 0.1);
        int finalPrice = discountedPrice + vat;

        log.info("[결제 계산] 원가: {}, 할인 후: {}, VAT: {}, 최종 결제 금액: {}", originalPrice, discountedPrice, vat, finalPrice);

        ModelPurchase purchase = ModelPurchase.builder()
                .user(user)
                .model(model)
                .paymentId(finalPrice > 0 ? UUID.randomUUID().toString() : null)
                .verified(finalPrice == 0)
                .coupon(appliedCoupon)
                .build();

        modelPurchaseRepository.save(purchase);

        if (finalPrice == 0) {
            log.info("[무료 결제] couponId={}, userId={} - 후처리 바로 실행", appliedCoupon != null ? appliedCoupon.getId() : "없음", userId);

            if (appliedCoupon != null) {
                couponService.markCouponAsUsed(appliedCoupon.getId(), userId);
            }

            model.setDownloadCount(model.getDownloadCount() + 1);
            modelRepository.save(model);

            user.setTotalPurchaseCount(user.getTotalPurchaseCount() + 1);
            user.setTotalPurchaseAmount(user.getTotalPurchaseAmount() + finalPrice);
            userService.updateUserLevel(user);
            userRepository.save(user);

            User seller = model.getAuthor().getUser();
            seller.setTotalSalesCount(seller.getTotalSalesCount() + 1);
            seller.setTotalSalesAmount(seller.getTotalSalesAmount() + finalPrice);
            userService.updateUserLevel(seller);
            userRepository.save(seller);
        }

        log.info("[도면 구매 요청 완료] userId={}, modelId={}, paymentId={}, verified={}",
                userId, model.getId(), purchase.getPaymentId(), purchase.isVerified());

        return UserModelDto.ModelResponse.from(purchase, model.isDownloadable());
    }

    public void forceDeleteModelByAdmin(Long modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        log.warn("[어드민 도면 강제 삭제] modelId={}, modelName={}", model.getId(), model.getName());

        model.setDeleted(true);
        modelRepository.save(model);
    }

    public List<UserModelDto.ModelResponse> getPopularByDownloadList(int page, Optional<Long> userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository
                .findAllByIsDeletedFalseOrderByDownloadCountDesc(pageable)
                .getContent();

        return models.stream()
                .map(model -> {
                    boolean purchaseAvailability = userId
                            .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, model.getId()))
                            .orElse(true);
                    return UserModelDto.ModelResponse.from(model, purchaseAvailability, model.isDownloadable());
                })
                .toList();
    }

    public List<UserModelDto.ModelResponse> getFreeModelList(int page, Optional<Long> userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository
                .findAllByIsDeletedFalseAndPriceEqualsOrderByCreateDateDesc(0L, pageable)
                .getContent();

        return models.stream()
                .map(model -> {
                    boolean purchaseAvailability = userId
                            .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, model.getId()))
                            .orElse(true);
                    return UserModelDto.ModelResponse.from(model, purchaseAvailability, model.isDownloadable());
                })
                .toList();
    }

    // ✅ 유료 도면 목록
    public List<UserModelDto.ModelResponse> getPaidModelList(int page, Optional<Long> userId) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Model> models = modelRepository
                .findAllByIsDeletedFalseAndPriceGreaterThanOrderByCreateDateDesc(0L, pageable)
                .getContent();

        return models.stream()
                .map(model -> {
                    boolean purchaseAvailability = userId
                            .map(uId -> !uId.equals(model.getAuthor().getId()) && !checkIfModelPurchased(uId, model.getId()))
                            .orElse(true);
                    return UserModelDto.ModelResponse.from(model, purchaseAvailability, model.isDownloadable());
                })
                .toList();
    }

    private boolean looksLikeStl(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        // 1) 이름 기반 (가끔 비어있거나 uuid일 수 있음)
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (name.endsWith(".stl")) return true;

        // 2) 컨텐트 타입 기반 (브라우저가 종종 octet-stream으로 보냄)
        String ct = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (ct.contains("stl") || ct.equals("application/sla") || ct.equals("model/stl") || ct.equals("application/octet-stream")) {
            return true;
        }

        // 3) 헤더 휴리스틱 (ASCII: "solid"로 시작 / Binary: 84바이트 이상)
        try (var in = file.getInputStream()) {
            byte[] header = in.readNBytes(512);
            if (header.length >= 5) {
                String headStr = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
                if (headStr.startsWith("solid")) return true; // ASCII STL
            }
            if (header.length >= 84) return true; // 바이너리 STL 최소 헤더
        } catch (Exception ignore) {}

        return false;
    }

    private void requireStlOrThrow(MultipartFile file) {
        if (!looksLikeStl(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "STL(.stl) 파일만 업로드할 수 있습니다.");
        }
    }
    private java.util.List<File> saveImages(java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        if (files == null || files.isEmpty()) return java.util.List.of();
        return files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(fileService::saveImage) // 기존 FileService.saveImage 사용
                .toList();
    }

}

