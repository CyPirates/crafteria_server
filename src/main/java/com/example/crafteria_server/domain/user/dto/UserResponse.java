package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    @Schema(description = "유저 ID", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "유레기")
    private String username;

    @Schema(description = "실명", example = "홍길동")
    private String realname;

    @Schema(description = "OAuth2 ID", example = "1234567890")
    private String oauth2Id;

    @Schema(description = "유저 권한", example = "ROLE_USER")
    private Role role;

    @Schema(description = "주소", example = "서울시 강남구")
    private String address;

    @Schema(description = "총 도면 구매 횟수", example = "3")
    private int totalPurchaseCount;

    @Schema(description = "총 도면 구매 금액", example = "15000")
    private long totalPurchaseAmount;

    @Schema(description = "총 도면 업로드 수", example = "5")
    private int totalUploadCount;

    @Schema(description = "총 도면 판매 횟수", example = "2")
    private int totalSalesCount;

    @Schema(description = "총 도면 판매 금액", example = "30000")
    private long totalSalesAmount;

    @Schema(description = "총 출력 주문 횟수", example = "1")
    private int totalPrintedCount;

    @Schema(description = "총 출력 주문 금액", example = "5000")
    private long totalPrintedAmount;

    @Schema(description = "유저 레벨 (1~5)", example = "3")
    private int userLevel;

    @Schema(description = "판매자 레벨(1~5)", example = "4")
    private int sellerLevel;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realname(user.getRealname())
                .oauth2Id(user.getOauth2Id())
                .role(user.getRole())
                .address(user.getAddress())
                .totalPurchaseCount(user.getTotalPurchaseCount())
                .totalPurchaseAmount(user.getTotalPurchaseAmount())
                .totalUploadCount(user.getTotalUploadCount())
                .totalSalesCount(user.getTotalSalesCount())
                .totalSalesAmount(user.getTotalSalesAmount())
                .totalPrintedCount(user.getTotalPrintedCount())
                .totalPrintedAmount(user.getTotalPrintedAmount())
                .userLevel(user.getUserLevel())
                .sellerLevel(user.getSellerLevel())
                .build();
    }
}
