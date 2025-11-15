package com.example.sharemyrecipe.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeListResponse<T> {
    private List<T> data;
    private PaginationMeta meta;

    @Data
    @Builder
    public static class PaginationMeta {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }
}