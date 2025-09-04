package com.example.crafteria_server.domain.identity.service;

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
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Slf4j(topic = "IdentityVerificationService")
@Service
@RequiredArgsConstructor
@Transactional
public class IdentityVerificationService {

    private final PortOneClient client;
    private final PortOneProps props;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PortOneClient.VerifiedIdentityDto fetchVerified(String identityVerificationId) {
        var dto = client.get(identityVerificationId, props.getStoreId()).block();
        if (dto == null || !"VERIFIED".equalsIgnoreCase(dto.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인인증이 완료되지 않았습니다.");
        }
        return dto.getIdentityVerification();
    }

    public void send(String identityVerificationId, PortOneClient.SendBody body) {
        if (body.getStoreId() == null) body.setStoreId(props.getStoreId());
        if (body.getChannelKey() == null) body.setChannelKey(props.getChannelKey());
        client.send(identityVerificationId, body).block();
    }

    public PortOneClient.VerifiedIdentityDto confirm(String id, String otp) {
        var body = new PortOneClient.ConfirmBody();
        body.setStoreId(props.getStoreId());
        body.setOtp(otp);
        return client.confirm(id, body).block();
    }

    /** ✅ 공통: 인증 결과를 User에 반영 */
    public void attachVerificationToUser(Long userId, String identityVerificationId, PortOneClient.VerifiedIdentityDto v) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 실명/전화번호 갱신
        user.setRealname(safe(v.getName()));
        user.setPhoneNumber(safe(v.getPhoneNumber()));

        // 민감정보 저장 (외부응답/로그 노출 금지)
        user.setCi(safe(v.getCi()));
        user.setDi(safe(v.getDi()));

        // 상태/시각/최근 인증ID
        user.setIdentityVerified(true);
        user.setIdentityVerifiedAt(parseVerifiedAt(v.getVerifiedAt()).orElse(LocalDateTime.now()));
        user.setLastIdentityVerificationId(identityVerificationId);

        userRepository.save(user);

        log.info("[본인인증 반영] userId={}, verified=true, ivId={}", userId, mask(identityVerificationId));
    }

    private String safe(String s) { return (s == null || s.isBlank()) ? null : s; }

    private Optional<LocalDateTime> parseVerifiedAt(String iso) {
        if (iso == null || iso.isBlank()) return Optional.empty();
        try {
            return Optional.of(OffsetDateTime.parse(iso).toLocalDateTime());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private String mask(String s) {
        if (s == null) return null;
        return s.length() <= 6 ? "***" : s.substring(0,3) + "****" + s.substring(s.length()-3);
    }
}
