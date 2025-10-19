package com.example.crafteria_server.domain.identity.service;

import com.example.crafteria_server.domain.identity.dto.VerifiedIdentityUnified;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.portone.PortOneClient;
import com.example.crafteria_server.global.portone.PortOneProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Slf4j(topic = "IdentityVerificationService")
@Service
@RequiredArgsConstructor
@Transactional
public class IdentityVerificationService {

    private final PortOneClient portOneClient;
    private final PortOneProps portOneProps;
    private final UserRepository userRepository;

    public VerifiedIdentityUnified fetchVerified(String identityVerificationId) {
        var dto = portOneClient.get(identityVerificationId, portOneProps.getStoreId()).block();
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인인증 응답이 비었습니다.");
        }
        if (!"VERIFIED".equalsIgnoreCase(dto.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "본인인증이 완료되지 않았습니다. (status=" + dto.getStatus() + ")");
        }

        // v1 우선
        if (dto.getIdentityVerification() != null) {
            var src = dto.getIdentityVerification();
            var v = new VerifiedIdentityUnified();
            v.setId(dto.getId());
            v.setStatus(dto.getStatus());
            v.setName(src.getName());
            v.setPhoneNumber(src.getPhoneNumber());
            v.setBirth(src.getBirth());              // yyyyMMdd
            v.setGender(src.getGender());
            v.setForeigner(src.getForeigner());
            v.setCi(src.getCi());
            v.setDi(src.getDi());
            v.setOperator(src.getOperator());
            v.setVerifiedAt(dto.getVerifiedAt());    // ISO string
            return v;
        }

        // v2 (현재 RAW 구조)
        if (dto.getVerifiedCustomer() != null) {
            var c = dto.getVerifiedCustomer();
            var v = new VerifiedIdentityUnified();
            v.setId(dto.getId());
            v.setStatus(dto.getStatus());
            v.setName(c.getName());
            v.setPhoneNumber(null);                  // v2 기본 응답에는 없을 수 있음
            v.setBirth(toYYYYMMDD(c.getBirthDate())); // 1997-10-21 -> 19971021
            v.setGender(c.getGender());
            v.setForeigner(null);
            v.setCi(c.getCi());
            v.setDi(c.getDi());
            v.setOperator(null);
            v.setVerifiedAt(dto.getVerifiedAt());    // ISO string
            return v;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "본인인증 상세 정보가 비어 있습니다. (v1/v2 매핑 불가)");
    }

    private String toYYYYMMDD(String birthDate) {
        return (birthDate == null) ? null : birthDate.replace("-", "");
    }

    /** ✅ 유저 엔티티 반영: realname/phoneNumber + ci/di + identityVerified(+At) + lastIdentityVerificationId */
    public void attachVerificationToUser(Long userId, String identityVerificationId, VerifiedIdentityUnified v) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 실명/전화번호 (v2에서는 전화번호가 없을 수 있음 → null-safe)
        if (v.getName() != null && !v.getName().isBlank()) {
            user.setRealname(v.getName());
        }
        if (v.getPhoneNumber() != null && !v.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(v.getPhoneNumber());
        }

        // ci/di 저장 (본인인증 구별용)
        if (v.getCi() != null && !v.getCi().isBlank()) {
            user.setCi(v.getCi());
        }
        if (v.getDi() != null && !v.getDi().isBlank()) {
            user.setDi(v.getDi());
        }

        // 인증 상태/시각/최근 인증ID
        user.setIdentityVerified(true);
        user.setLastIdentityVerificationId(identityVerificationId);
        user.setIdentityVerifiedAt(parseVerifiedAtToSeoul(v.getVerifiedAt())); // ISO → Asia/Seoul LocalDateTime

        userRepository.save(user);

        log.info("[본인인증 반영] userId={}, verified=true, ivId={}",
                userId, maskId(identityVerificationId));
    }

    /** PortOne ISO 시각(예: 2025-09-11T06:17:44.66144026Z) → Asia/Seoul LocalDateTime */
    private LocalDateTime parseVerifiedAtToSeoul(String iso) {
        if (iso == null || iso.isBlank()) return null;
        ZonedDateTime zdt = ZonedDateTime.parse(iso);                 // Z(UTC) 기반
        return zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }

    private String maskId(String id) {
        if (id == null || id.length() < 6) return id;
        return id.substring(0, 3) + "****" + id.substring(id.length() - 3);
    }
}
