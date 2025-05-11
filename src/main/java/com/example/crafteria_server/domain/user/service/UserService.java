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
import lombok.extern.slf4j.Slf4j;
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

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j(topic = "UserService")
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public void registerDashboardUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .realname(request.getRealname())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DASHBOARD)
                .dashboardStatus(DashboardStatus.PENDING) // 일반 회원가입만 PENDING 설정
                .manufacturerName(request.getManufacturerName())
                .manufacturerDescription(request.getManufacturerDescription())
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

        if (user.getRole() == Role.DASHBOARD && user.getDashboardStatus() != DashboardStatus.APPROVED) {
            throw new UsernameNotFoundException("승인되지 않은 계정입니다.");
        }

        return new PrincipalDetails(user);
    }

    public LoginDto.LoginResponse login(LoginDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getBanUntil() != null && user.getBanUntil().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "계정이 정지된 상태입니다. 정지 해제일: " + user.getBanUntil());
        }

        if (user.getRole() == Role.DASHBOARD && user.getDashboardStatus() != DashboardStatus.APPROVED) {
            throw new UsernameNotFoundException("승인되지 않은 계정입니다.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(user), null, new PrincipalDetails(user).getAuthorities());
        String accessToken = tokenProvider.generateAccessToken(authentication);

        String manufacturerId = Optional.ofNullable(user.getManufacturer())
                .filter(m -> user.getDashboardStatus() == DashboardStatus.APPROVED)
                .map(m -> m.getId().toString())
                .or(() -> Optional.ofNullable(user.getManufacturerName()))
                .orElse("제조사 등록이 안됐습니다.");

        return LoginDto.LoginResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .manufacturerId(manufacturerId)
                .build();
    }

    public String getLoggedInUserManufacturerId(PrincipalDetails principalDetails) {
        User user = userRepository.findById(principalDetails.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("로그인된 유저를 찾을 수 없습니다."));

        return Optional.ofNullable(user.getManufacturer())
                .map(manufacturer -> manufacturer.getId().toString())
                .orElse("대시보드 유저에게 매칭된 제조사가 없습니다.");
    }

    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUsername(username);
    }

    public void deleteUser(Long userId, PrincipalDetails principalDetails) throws AccessDeniedException {
        if (principalDetails.getUser().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("ADMIN 권한이 필요합니다.");
        }
        userRepository.deleteById(userId);
    }

    public void banUser(Long userId, LocalDateTime until, PrincipalDetails principalDetails) throws AccessDeniedException {
        if (principalDetails.getUser().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("ADMIN 권한이 필요합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        user.setBanUntil(until);
        userRepository.save(user);
    }

    public void updateUserLevel(User user) {
        int prevUserLevel = user.getUserLevel();
        int prevSellerLevel = user.getSellerLevel();

        int newUserLevel = 0;
        int newSellerLevel = 0;

        // 일반 사용자 레벨 기준
        if (user.getTotalPurchaseAmount() >= 10000 || user.getTotalPrintedAmount() >= 5000) {
            newUserLevel = 5;
        } else if (user.getTotalPurchaseAmount() >= 5000 || user.getTotalPrintedAmount() >= 2500) {
            newUserLevel = 4;
        } else if (user.getTotalPurchaseAmount() >= 2500 || user.getTotalPrintedAmount() >= 1250) {
            newUserLevel = 3;
        } else if (user.getTotalPurchaseAmount() >= 1250 || user.getTotalPrintedAmount() >= 625) {
            newUserLevel = 2;
        } else if (user.getTotalPurchaseAmount() >= 1 || user.getTotalPrintedAmount() >= 1) {
            newUserLevel = 1;
        }

        // 판매자 레벨 기준
        if (user.getTotalSalesAmount() >= 100000 || user.getTotalUploadCount() >= 100) {
            newSellerLevel = 5;
        } else if (user.getTotalSalesAmount() >= 50000 || user.getTotalUploadCount() >= 50) {
            newSellerLevel = 4;
        } else if (user.getTotalSalesAmount() >= 25000 || user.getTotalUploadCount() >= 25) {
            newSellerLevel = 3;
        } else if (user.getTotalSalesAmount() >= 12500 || user.getTotalUploadCount() >= 10) {
            newSellerLevel = 2;
        } else if (user.getTotalSalesAmount() >= 1 || user.getTotalUploadCount() >= 1) {
            newSellerLevel = 1;
        }

        user.setUserLevel(newUserLevel);
        user.setSellerLevel(newSellerLevel);

        if (newUserLevel > prevUserLevel) {
            log.info("🎉 일반 유저 레벨업 - 유저ID: {}, 이름: {}, {} → {}", user.getId(), user.getUsername(), prevUserLevel, newUserLevel);
        } else if (newUserLevel < prevUserLevel) {
            log.info("📉 일반 유저 레벨다운 - 유저ID: {}, 이름: {}, {} → {}", user.getId(), user.getUsername(), prevUserLevel, newUserLevel);
        }

        if (newSellerLevel > prevSellerLevel) {
            log.info("🎉 판매자 레벨업 - 유저ID: {}, 이름: {}, {} → {}", user.getId(), user.getUsername(), prevSellerLevel, newSellerLevel);
        } else if (newSellerLevel < prevSellerLevel) {
            log.info("📉 판매자 레벨다운 - 유저ID: {}, 이름: {}, {} → {}", user.getId(), user.getUsername(), prevSellerLevel, newSellerLevel);
        }
    }

}
