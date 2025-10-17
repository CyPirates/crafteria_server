package com.example.crafteria_server.domain.user.service;

import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
import com.example.crafteria_server.domain.user.dto.UserAddressDto;
import com.example.crafteria_server.domain.user.dto.UserBankAccountDto;
import com.example.crafteria_server.domain.user.dto.UserResponse;
import com.example.crafteria_server.domain.user.dto.UserUpdateRequest;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.ModelSaleTransaction;
import com.example.crafteria_server.domain.user.repository.UserAddressRepository;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "UserInfoService")
@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserRepository userRepository;
    private final ModelPurchaseRepository modelPurchaseRepository;
    private final UserAddressRepository userAddressRepository;

    // 로그인한 사용자의 정보를 반환
    @Transactional
    public User getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getBanUntil() != null && user.getBanUntil().isBefore(LocalDateTime.now())) {
            user.setBanned(false);
            user.setBanUntil(null);
            userRepository.save(user);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ 공통 응답 생성
    public UserResponse toUserResponse(User user) {
        List<UserAddressDto.UserAddressResponse> addresses = userAddressRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(UserAddressDto.UserAddressResponse::from)
                .toList();

        List<ModelSaleTransaction> sales = modelPurchaseRepository
                .findVerifiedPurchasesByModelAuthor(user.getId())
                .stream()
                .map(ModelSaleTransaction::from)
                .toList();

        return UserResponse.from(user, addresses, sales);
    }

    @Transactional
    public void upsertBankAccount(Long userId, UserBankAccountDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 정규화(공백 제거 등) — 필요 시 강화
        String normalized = request.getAccountNumber()
                .replaceAll("\\s+", "") // 공백 제거
                .replaceAll("-", "-");   // 특수 하이픈 -> 일반 하이픈

        user.setBankAccount(normalized); // ✅ 컨버터가 암호화해서 DB 저장
        userRepository.save(user);
        log.info("[계좌 저장] userId={}, account(normalized)={}", userId, normalized);
    }

    @Transactional
    public void deleteBankAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.setBankAccount(null);
        userRepository.save(user);
        log.info("[계좌 삭제] userId={}", userId);
    }


}
