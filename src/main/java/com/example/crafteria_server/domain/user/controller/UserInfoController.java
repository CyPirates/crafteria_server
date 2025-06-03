package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.UserAddressDto;
import com.example.crafteria_server.domain.user.dto.UserResponse;
import com.example.crafteria_server.domain.user.dto.UserUpdateRequest;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.service.UserInfoService;
import com.example.crafteria_server.domain.user.service.UserService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import com.google.api.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j(topic = "UserInfoController")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;
    private final UserService userService;


    // 로그인한 사용자 정보 조회 API
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public JsonBody<UserResponse> getCurrentUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        User user = userInfoService.getCurrentUser(principalDetails.getUserId());
        List<UserAddressDto.UserAddressResponse> addresses = userService.getUserAddresses(user.getId());
        return JsonBody.of(200, "성공", UserResponse.from(user, addresses));
    }

    @PatchMapping("/me")
    @Operation(summary = "현재 사용자 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    public JsonBody<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UserUpdateRequest request) {

        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        log.info("회원정보 수정 요청 - 유저ID: {}, 변경 정보: username={}, realname={}",
                principalDetails.getUserId(), request.getUsername(), request.getRealname());

        userService.updateBasicUserInfo(principalDetails.getUserId(), request);
        User updatedUser = userInfoService.getCurrentUser(principalDetails.getUserId());
        List<UserAddressDto.UserAddressResponse> addresses = userService.getUserAddresses(updatedUser.getId());
        return JsonBody.of(200, "성공", UserResponse.from(updatedUser, addresses));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "특정 사용자 조회", description = "특정 사용자의 정보를 조회합니다.")
    public JsonBody<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userInfoService.getUserById(userId);
        List<UserAddressDto.UserAddressResponse> addresses = userService.getUserAddresses(user.getId());
        return JsonBody.of(200, "성공", UserResponse.from(user, addresses));
    }

    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "모든 사용자의 정보를 조회합니다.")
    public JsonBody<List<UserResponse>> getAllUsers() {
        List<User> users = userInfoService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(user -> UserResponse.from(user, userService.getUserAddresses(user.getId())))
                .toList();
        return JsonBody.of(200, "성공", responses);
    }

    @PostMapping("/me/address")
    @Operation(summary = "주소 추가", description = "현재 로그인한 사용자 주소를 추가합니다.")
    public JsonBody<UserAddressDto.UserAddressResponse> addAddress(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UserAddressDto.UserAddressRequest request
    ) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        UserAddressDto.UserAddressResponse response = userService.addUserAddress(principalDetails.getUserId(), request);
        return JsonBody.of(200, "주소가 추가되었습니다.", response);
    }

    @PatchMapping("/me/address/{addressId}")
    @Operation(summary = "주소 수정", description = "현재 로그인한 사용자의 주소 정보를 수정합니다.")
    public JsonBody<UserAddressDto.UserAddressResponse> updateAddress(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long addressId,
            @RequestBody @Valid UserAddressDto.UserAddressRequest request
    ) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        UserAddressDto.UserAddressResponse response = userService.updateUserAddress(principalDetails.getUserId(), addressId, request);
        return JsonBody.of(200, "주소가 수정되었습니다.", response);
    }

    @DeleteMapping("/me/address/{addressId}")
    @Operation(summary = "주소 삭제", description = "현재 로그인한 사용자의 주소를 삭제합니다.")
    public JsonBody<String> deleteAddress(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long addressId
    ) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        userService.deleteUserAddress(principalDetails.getUserId(), addressId);
        return JsonBody.of(200, "주소가 삭제되었습니다.",null);
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "주소 목록 조회", description = "현재 로그인한 사용자의 주소 목록을 조회합니다.")
    public JsonBody<List<UserAddressDto.UserAddressResponse>> getMyAddresses(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        List<UserAddressDto.UserAddressResponse> addresses = userService.getUserAddresses(principalDetails.getUserId());
        return JsonBody.of(200, "성공", addresses);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 삭제", description = "특정 사용자를 삭제합니다.")
    public JsonBody<String> deleteUser(@PathVariable Long userId, @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {
        userService.deleteUser(userId, principalDetails);
        return JsonBody.of(200, "사용자가 삭제되었습니다.", null);
    }

    @PostMapping("/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 정지", description = "특정 사용자를 정지합니다.")
    public JsonBody<String> banUser(@PathVariable Long userId,
                                    @RequestParam("until") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until,
                                    @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {
        userService.banUser(userId, until, principalDetails);
        return JsonBody.of(200, "사용자가 " + until + "까지 정지되었습니다.", null);
    }
}
