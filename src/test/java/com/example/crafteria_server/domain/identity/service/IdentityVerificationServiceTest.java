package com.example.crafteria_server.domain.identity.service;

import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.portone.PortOneClient;
import com.example.crafteria_server.global.portone.PortOneProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IdentityVerificationServiceTest {

    @Mock
    private PortOneClient portOneClient;

    @Mock
    private UserRepository userRepository;

    private PortOneProps props;
    private IdentityVerificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new PortOneProps();
        props.setApiSecret("test-secret");
        props.setStoreId("store-1234");
        props.setChannelKey("chan-1234");

        service = new IdentityVerificationService(portOneClient, props, userRepository);
    }

    private User dummyUser(Long id) {
        // 필요한 최소 필드만 채워도 충분
        User u = User.builder()
                .id(id)
                .username("tester")
                .realname(null)
                .phoneNumber(null)
                .identityVerified(false)
                .identityVerifiedAt(null)
                .build();
        return u;
    }

    private PortOneClient.VerifiedIdentityDto verifiedFixture() {
        PortOneClient.VerifiedIdentityDto v = new PortOneClient.VerifiedIdentityDto();
        v.setId("iv-abc");
        v.setStatus("VERIFIED");
        v.setName("홍길동");
        v.setPhoneNumber("01012345678");
        v.setBirth("19900101");
        v.setGender("MALE");
        v.setForeigner(false);
        v.setCi("CI-EXAMPLE");
        v.setDi("DI-EXAMPLE");
        v.setOperator("SKT");
        v.setVerifiedAt("2025-08-18T12:34:56+09:00");
        return v;
    }

    private PortOneClient.IdentityVerificationDto wrapAsVerified(PortOneClient.VerifiedIdentityDto v) {
        PortOneClient.IdentityVerificationDto dto = new PortOneClient.IdentityVerificationDto();
        dto.setId(v.getId());
        dto.setStatus("VERIFIED");
        dto.setIdentityVerification(v);
        dto.setFailure(null);
        return dto;
    }

    @Nested
    @DisplayName("SDK 플로우 (fetchVerified → attachVerificationToUser)")
    class SdkFlow {

        @Test
        @DisplayName("status=VERIFIED면 User에 realname/phone/ci/di 저장 & 플래그/시각 갱신")
        void fetchVerified_thenAttach_success() {
            // given
            Long userId = 1L;
            String ivId = "iv-abc";

            var user = dummyUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            var v = verifiedFixture();
            var dto = wrapAsVerified(v);
            when(portOneClient.get(eq(ivId), any())).thenReturn(Mono.just(dto));

            // when
            var result = service.fetchVerified(ivId);
            service.attachVerificationToUser(userId, ivId, result);

            // then (save된 유저 캡처)
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(captor.capture());
            User saved = captor.getValue();

            assertThat(result.getStatus()).isEqualTo("VERIFIED");
            assertThat(saved.getRealname()).isEqualTo("홍길동");
            assertThat(saved.getPhoneNumber()).isEqualTo("01012345678");
            assertThat(saved.getCi()).isEqualTo("CI-EXAMPLE");
            assertThat(saved.getDi()).isEqualTo("DI-EXAMPLE");
            assertThat(saved.isIdentityVerified()).isTrue();
            assertThat(saved.getIdentityVerifiedAt()).isNotNull();
            assertThat(saved.getLastIdentityVerificationId()).isEqualTo(ivId);
        }

        @Test
        @DisplayName("status!=VERIFIED면 ResponseStatusException 발생")
        void fetchVerified_fail() {
            // given
            String ivId = "iv-fail";
            PortOneClient.IdentityVerificationDto dto = new PortOneClient.IdentityVerificationDto();
            dto.setId(ivId);
            dto.setStatus("FAILED");
            when(portOneClient.get(eq(ivId), any())).thenReturn(Mono.just(dto));

            // when & then
            assertThatThrownBy(() -> service.fetchVerified(ivId))
                    .isInstanceOf(ResponseStatusException.class);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("서버발송 플로우 (confirm → attachVerificationToUser)")
    class ServerSendFlow {

        @Test
        @DisplayName("confirm 성공 시 User에 정보 반영")
        void confirm_thenAttach_success() {
            // given
            Long userId = 10L;
            String ivId = "iv-xyz";
            String otp = "123456";

            var user = dummyUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            var v = verifiedFixture();
            when(portOneClient.confirm(eq(ivId), any())).thenReturn(Mono.just(v));

            // when
            var confirmed = service.confirm(ivId, otp);
            service.attachVerificationToUser(userId, ivId, confirmed);

            // then
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getRealname()).isEqualTo("홍길동");
            assertThat(saved.getPhoneNumber()).isEqualTo("01012345678");
            assertThat(saved.getCi()).isEqualTo("CI-EXAMPLE");
            assertThat(saved.getDi()).isEqualTo("DI-EXAMPLE");
            assertThat(saved.isIdentityVerified()).isTrue();
            assertThat(saved.getIdentityVerifiedAt()).isNotNull();
            assertThat(saved.getLastIdentityVerificationId()).isEqualTo(ivId);
        }
    }

    @Test
    @DisplayName("send 호출은 PortOneClient.send가 1회 호출되면 성공으로 간주")
    void send_success() {
        // given
        String ivId = "iv-send";
        PortOneClient.SendBody body = new PortOneClient.SendBody();
        body.setStoreId(null); // 서비스에서 props로 보충
        body.setChannelKey(null);
        PortOneClient.SendBody.Customer c = new PortOneClient.SendBody.Customer();
        c.setName("홍길동");
        c.setPhoneNumber("01011112222");
        c.setBirth("19990101");
        c.setGender("MALE");
        c.setForeigner(false);
        body.setCustomer(c);
        body.setOperator("SKT");
        body.setMethod("SMS");

        when(portOneClient.send(eq(ivId), any())).thenReturn(Mono.empty());

        // when
        service.send(ivId, body);

        // then
        verify(portOneClient, times(1)).send(eq(ivId), any(PortOneClient.SendBody.class));
    }
}
