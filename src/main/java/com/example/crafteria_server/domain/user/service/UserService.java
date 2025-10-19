package com.example.crafteria_server.domain.user.service;

import com.example.crafteria_server.domain.user.dto.*;
import com.example.crafteria_server.domain.user.entity.*;
import com.example.crafteria_server.domain.user.repository.AuthorRepository;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.security.PrincipalDetails;
import com.example.crafteria_server.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "UserService")
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthorRepository authorRepository;

    public void registerDashboardUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .realname(request.getRealname())
                .phoneNumber(request.getPhoneNumber())
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


        if (user.getRole() == Role.DASHBOARD && user.getDashboardStatus() != DashboardStatus.APPROVED) {
            throw new UsernameNotFoundException("승인되지 않은 계정입니다.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(user), null, new PrincipalDetails(user).getAuthorities());
        String accessToken = tokenProvider.generateAccessToken(authentication);

        String manufacturerId = Optional.ofNullable(user.getManufacturer())
                .filter(m -> user.getDashboardStatus() == DashboardStatus.APPROVED)
                .map(m -> m.getId().toString())
                .orElse(null);

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
        user.setBanned(true); // ✅ banned true 설정
        userRepository.save(user);
    }

    public void updateUserLevel(User user) {
        int prevUserLevel = user.getUserLevel();
        int prevSellerLevel = user.getSellerLevel();

        int newUserLevel = 0;
        int newSellerLevel = 0;

        // 일반 사용자 레벨 기준
        if (user.getTotalPurchaseAmount() >= 1000000 || user.getTotalPrintedAmount() >= 100) {
            newUserLevel = 5;
        } else if (user.getTotalPurchaseAmount() >= 750000 || user.getTotalPrintedAmount() >= 75) {
            newUserLevel = 4;
        } else if (user.getTotalPurchaseAmount() >= 500000 || user.getTotalPrintedAmount() >= 50) {
            newUserLevel = 3;
        } else if (user.getTotalPurchaseAmount() >= 100000  || user.getTotalPrintedAmount() >= 10) {
            newUserLevel = 2;
        } else if (user.getTotalPurchaseAmount() >= 1000 || user.getTotalPrintedAmount() >= 1) {
            newUserLevel = 1;
        }

        // 판매자 레벨 기준
        if (user.getTotalSalesAmount() >= 10000000 || user.getTotalPrintedAmount() >= 50000000 ||
                user.getTotalPrintedCount() >= 1000 || user.getTotalSalesCount() >= 1000 || user.getTotalUploadCount() >=200 ) {
            newSellerLevel = 5;
        } else if (user.getTotalSalesAmount() >= 5000000 || user.getTotalPrintedAmount() >= 25000000 ||
                user.getTotalPrintedCount() >= 500 || user.getTotalSalesCount() >= 500 || user.getTotalUploadCount() >=100) {
            newSellerLevel = 4;
        } else if (user.getTotalSalesAmount() >= 2500000 || user.getTotalPrintedAmount() >= 12500000 ||
                user.getTotalPrintedCount() >= 250 || user.getTotalSalesCount() >= 250 || user.getTotalUploadCount() >=50) {
            newSellerLevel = 3;
        } else if (user.getTotalSalesAmount() >= 1000000 || user.getTotalPrintedAmount() >= 1000000 ||
                user.getTotalPrintedCount() >= 100 || user.getTotalSalesCount() >= 100 || user.getTotalUploadCount() >=25) {
            newSellerLevel = 2;
        } else if (user.getTotalSalesAmount() >= 1000 || user.getTotalPrintedAmount() >= 1000 ||
                user.getTotalPrintedCount() >= 1 || user.getTotalSalesCount() >= 1 || user.getTotalUploadCount() >=1) {
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

    public UserAddressDto.UserAddressResponse updateUserAddress(Long userId, Long addressId, UserAddressDto.UserAddressRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        UserAddress address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."));

        if (dto.isDefault()) {
            user.getAddresses().forEach(a -> a.setDefault(false));
            address.setDefault(true);
        }

        address.setLabel(dto.getLabel());
        address.setBaseAddress(dto.getBaseAddress());
        address.setDetailAddress(dto.getDetailAddress());
        address.setPostalCode(dto.getPostalCode());

        userRepository.save(user);

        log.info("✏️ 주소 수정 - 유저ID: {}, 주소ID: {}, 라벨: {}, 기본여부: {}, 주소: {} {}", userId, addressId, dto.getLabel(), dto.isDefault(), dto.getBaseAddress(), dto.getDetailAddress());

        return UserAddressDto.UserAddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .baseAddress(address.getBaseAddress())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.isDefault())
                .postalCode(address.getPostalCode())
                .build();
    }

    public void deleteUserAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        user.getAddresses().removeIf(address -> address.getId().equals(addressId));
        userRepository.save(user);

        log.info("🗑️ 주소 삭제 - 유저ID: {}, 주소ID: {}", userId, addressId);
    }

    public List<UserAddressDto.UserAddressResponse> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        return user.getAddresses().stream()
                .map(a -> UserAddressDto.UserAddressResponse.builder()
                        .id(a.getId())
                        .label(a.getLabel())
                        .baseAddress(a.getBaseAddress())
                        .detailAddress(a.getDetailAddress())
                        .isDefault(a.isDefault())
                        .postalCode(a.getPostalCode())
                        .build())
                .toList();
    }

    public void updateBasicUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        user.setUsername(request.getUsername());
        user.setRealname(request.getRealname());

        userRepository.save(user);

        log.info("👤 사용자 정보 수정 - 유저ID: {}, 이름: {}, 실명: {}", userId, request.getUsername(), request.getRealname());
    }

    public UserAddressDto.UserAddressResponse addUserAddress(Long userId, UserAddressDto.UserAddressRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if (dto.isDefault()) {
            user.getAddresses().forEach(addr -> addr.setDefault(false));
        }

        UserAddress address = UserAddress.builder()
                .label(dto.getLabel())
                .baseAddress(dto.getBaseAddress())
                .detailAddress(dto.getDetailAddress())
                .isDefault(dto.isDefault())
                .user(user)
                .postalCode(dto.getPostalCode())
                .build();

        user.getAddresses().add(address);
        userRepository.save(user);

        log.info("📌 주소 추가 - 유저ID: {}, 라벨: {}, 기본여부: {}, 주소: {} {}", userId, dto.getLabel(), dto.isDefault(), dto.getBaseAddress(), dto.getDetailAddress());

        return UserAddressDto.UserAddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .baseAddress(address.getBaseAddress())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.isDefault())
                .postalCode(address.getPostalCode())
                .build();
    }

    public User checkAndLiftBan(User user) {
        if (user.isBanned()
                && user.getBanUntil() != null
                && user.getBanUntil().isBefore(LocalDateTime.now())) {
            user.setBanUntil(null);
            user.setBanned(false);
            userRepository.save(user);
        }
        return user;
    }

    @Transactional
    public void updateBasicUserInfo(Long userId, UserBasicInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // username 중복 체크 (자기 자신 제외)
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다.");
        }

        // phoneNumber 중복 체크 (자기 자신 제외)
        if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다.");
        }

        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
    }

    public List<PopularAuthorDto> getPopularAuthorsBySales(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Author> result = authorRepository.findPopularAuthorsBySales(pageable);

        List<PopularAuthorDto> list = result.getContent().stream()
                .map(PopularAuthorDto::from)
                .toList();

        log.info("[인기 작가 조회] page={}, size={}, returned={}", page, pageable.getPageSize(), list.size());
        return list;
    }



}
