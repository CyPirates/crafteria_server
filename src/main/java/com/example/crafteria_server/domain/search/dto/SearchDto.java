package com.example.crafteria_server.domain.search.dto;

import lombok.*;

public class SearchDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchRequestDto {
        private String keyword;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchResultDto {
        private String type; // 'Manufacturer', 'Author', 'Model'
        private Long id;
        private String title;
        private String description;
        private String additionalInfo;
    }
}
