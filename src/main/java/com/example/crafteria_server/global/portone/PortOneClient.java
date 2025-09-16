package com.example.crafteria_server.global.portone;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micrometer.common.lang.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PortOneClient {
    private final WebClient portOneWebClient;
    private final PortOneProps props;

    private void applyAuth(HttpHeaders h) {
        h.set("Authorization", "PortOne " + props.getApiSecret());
        h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /** (옵션) 서버발송: 본인인증 요청 전송 */
    public Mono<Void> send(String identityVerificationId, SendBody body) {
        return portOneWebClient.post()
                .uri("/identity-verifications/{id}/send", identityVerificationId)
                .headers(this::applyAuth)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /** (옵션) 서버발송: OTP 확인 */
    public Mono<VerifiedIdentityDto> confirm(String id, ConfirmBody body) {
        return portOneWebClient.post()
                .uri("/identity-verifications/{id}/confirm", id)
                .headers(this::applyAuth)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(VerifiedIdentityDto.class);
    }

    /** 프론트 SDK 완료 후: 결과 조회 */
    public Mono<IdentityVerificationDto> get(String id, @Nullable String storeId) {
        return portOneWebClient.get()
                .uri(uri -> uri.path("/identity-verifications/{id}")
                        .queryParamIfPresent("storeId", Optional.ofNullable(storeId))
                        .build(id))
                .headers(this::applyAuth)
                .retrieve()
                .bodyToMono(IdentityVerificationDto.class);
    }

    public Mono<String> getRaw(String id, @Nullable String storeId) {
        return portOneWebClient.get()
                .uri(uri -> uri.path("/identity-verifications/{id}")
                        .queryParamIfPresent("storeId", Optional.ofNullable(storeId))
                        .build(id))
                .headers(this::applyAuth)
                .retrieve()
                .bodyToMono(String.class);
    }

    /* ===== DTOs ===== */

    @Data
    public static class SendBody {
        private String storeId;
        private String channelKey;
        private Customer customer;
        private String operator; // SKT/KT/LGU+/MVNO
        private String method;   // SMS/APP
        private String customData;

        @Data public static class Customer {
            private String name;
            private String phoneNumber;
            private String birth;     // YYYYMMDD
            private String gender;    // MALE/FEMALE (PG별 스펙)
            private Boolean foreigner;
        }
    }

    @Data
    public static class ConfirmBody {
        private String storeId;
        private String otp;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdentityVerificationDto {
        private String id;
        private String status;

        // v1 (과거 응답)
        @JsonAlias({"identityVerification", "identity_verification"})
        private VerifiedIdentityDto identityVerification;

        // v2 (현재 응답)
        private VerifiedCustomer verifiedCustomer;

        @JsonAlias({"verifiedAt", "verified_at"})
        private String verifiedAt;

        // 필요 시 기타 필드들…
        private Object channel;
        private String pgTxId;
        private String pgRawResponse;
        private String version;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiedCustomer {
        private String id;
        private String name;
        private String birthDate;   // "1997-10-21"
        private String gender;      // "MALE" / "FEMALE" …
        private String ci;
        private String di;
    }

    /** 서비스에서 최종적으로 쓰는 통합 형태(우리 프로젝트 내부용) */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiedIdentityDto {
        private String id;            // identity-verification-...
        private String status;        // VERIFIED
        private String name;
        private String phoneNumber;   // v2에는 없을 수 있음 → null 허용
        private String birth;         // "19971021" 로 변환
        private String gender;
        private Boolean foreigner;    // v2 응답에 없으면 null 허용
        private String ci;
        private String di;
        private String operator;      // 없으면 null 허용
        private String verifiedAt;    // iso string
    }
}
