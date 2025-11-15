package com.example.sharemyrecipe.service;


import com.example.sharemyrecipe.dto.RecipeCreateDto;
import com.example.sharemyrecipe.dto.RecipeListResponse;
import com.example.sharemyrecipe.dto.RecipeResponseDto;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.UUID;

public interface RecipeService {
    RecipeResponseDto createRecipe(UUID chefId, RecipeCreateDto dto, MultipartFile[] images);
    RecipeResponseDto updateRecipe(UUID chefId, UUID recipeId, RecipeCreateDto dto);
    void deleteRecipe(UUID chefId, UUID recipeId);
    RecipeResponseDto getRecipe(UUID id, UUID requestingUserId);
    RecipeListResponse<RecipeResponseDto> listPublicRecipes(String q, Instant publishedFrom, Instant publishedTo,
                                                            UUID chefId, String chefHandle, Pageable pageable);
}