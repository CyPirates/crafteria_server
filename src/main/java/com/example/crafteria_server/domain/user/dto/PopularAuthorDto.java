package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.user.entity.Author;
import com.example.crafteria_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Optional;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularAuthorDto {
    @Schema(description = "작가 ID", example = "1")
    private Long authorId;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저 이름", example = "john_doe")
    private String username;

    @Schema(description = "작가 실명", example = "John Doe")
    private String realname;

    @Schema(description = "프로필 이미지 URL", example = "http://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "판매자 레벨", example = "3")
    private int sellerLevel;

    @Schema(description = "총 판매 횟수", example = "100")
    private long totalSalesCount;

    @Schema(description = "총 판매 금액", example = "500000")
    private long totalSalesAmount;

    @Schema(description = "모델 수", example = "50")
    private long modelCount;

    public static PopularAuthorDto from(Author a) {
        User u = a.getUser();
        String profileUrl = Optional.ofNullable(a.getProfileImage()).map(File::getUrl).orElse(null);

        return PopularAuthorDto.builder()
                .authorId(a.getId())
                .userId(u.getId())
                .username(u.getUsername())
                .realname(a.getRealname())
                .profileImageUrl(profileUrl)
                .sellerLevel(u.getSellerLevel())
                .totalSalesCount(u.getTotalSalesCount())
                .totalSalesAmount(u.getTotalSalesAmount())
                .modelCount(a.getModelCount())
                .build();
    }
}
