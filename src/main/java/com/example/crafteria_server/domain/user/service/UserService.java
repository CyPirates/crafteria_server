package com.example.crafteria_server.domain.user.service;

import com.example.crafteria_server.domain.user.dto.LoginDto;
import com.example.crafteria_server.domain.user.dto.RegisterRequest;
import com.example.crafteria_server.domain.user.entity.DashboardStatus;
import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.security.PrincipalDetails;
import com.example.crafteria_server.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public void registerDashboardUser(RegisterRequest request) {
        // 중복 아이디 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // PENDING 상태로 대시보드 계정 생성
        User newUser = User.builder()
                .username(request.getUsername())
                .realname(request.getRealname())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DASHBOARD)
                .dashboardStatus(DashboardStatus.PENDING) // 기본값 대기 상태
                .build();

        userRepository.save(newUser);
    }

    public void updateDashboardStatus(Long userId, DashboardStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.DASHBOARD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대시보드 유저만 상태를 변경할 수 있습니다.");
        }

        user.setDashboardStatus(status);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 대시보드 유저는 APPROVED 상태만 허용
        if (user.getRole() == Role.DASHBOARD && user.getDashboardStatus() != DashboardStatus.APPROVED) {
            throw new UsernameNotFoundException("승인되지 않은 계정입니다.");
        }

        return new PrincipalDetails(user);
    }

    public LoginDto.LoginResponse login(LoginDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(user), null, new PrincipalDetails(user).getAuthorities());
        String accessToken = tokenProvider.generateAccessToken(authentication);

        return LoginDto.LoginResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .build();
    }
}
