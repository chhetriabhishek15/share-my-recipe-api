package com.example.sharemyrecipe.dto;

import com.example.sharemyrecipe.core.enums.RecipeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class RecipeResponseDto {
    private UUID id;
    private String title;
    private String summary;
    private String ingredients;
    private String steps;
    private Set<String> labels;
    private RecipeStatus status;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID chefId;
    private String chefHandle;
    private Map<UUID, Map<String, String>> images;
}