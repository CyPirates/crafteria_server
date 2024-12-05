package com.example.crafteria_server.global.security;

import com.example.crafteria_server.domain.user.dto.OAuth2UserInfo;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        boolean isDashboard = determineIfDashboard(); // 대시보드 여부 결정 로직
        User user = getOrSave(oAuth2UserInfo, isDashboard);

        return new PrincipalDetails(user, oAuth2UserAttributes);
    }

    private User getOrSave(OAuth2UserInfo oAuth2UserInfo, boolean isDashboard) {
        User user = userRepository.findByOauth2Id(oAuth2UserInfo.email())
                .orElseGet(() -> oAuth2UserInfo.toEntity(isDashboard));
        return userRepository.save(user);
    }

    private boolean determineIfDashboard() {
        // API 요청에서 대시보드 여부를 확인하는 로직 추가
        return false; // 기본값: 필요 시 API 요청 처리
    }
}
