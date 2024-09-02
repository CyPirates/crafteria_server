package com.example.crafteria_server.domain.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "item registeration", description = "아이템 등록", type = "multipartForm")
public class FileDto {
    @Schema(name = "image", description = "플레이리스트 이미지입니다.", type = "string", format = "binary")
    private MultipartFile image;
}
