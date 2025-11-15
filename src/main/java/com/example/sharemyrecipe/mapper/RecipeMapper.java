package com.example.sharemyrecipe.mapper;

import com.example.sharemyrecipe.dto.RecipeResponseDto;
import com.example.sharemyrecipe.entity.Recipe;
import com.example.sharemyrecipe.entity.RecipeImage;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    public RecipeResponseDto toDto(Recipe recipe) {

        // Single builder — defined only once
        RecipeResponseDto.RecipeResponseDtoBuilder b = RecipeResponseDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .summary(recipe.getSummary())
                .ingredients(recipe.getIngredients())
                .steps(recipe.getSteps())
                .labels(recipe.getLabels())
                .status(recipe.getStatus())
                .publishedAt(recipe.getPublishedAt())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .chefId(recipe.getChef() != null ? recipe.getChef().getId() : null)
                .chefHandle(recipe.getChef() != null ? recipe.getChef().getHandle() : null);

        // SAFELY map images
        if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {

            Map<UUID, Map<String, String>> imgs = recipe.getImages().stream()
                    .filter(img -> img.getId() != null)  // avoid null keys
                    .collect(Collectors.toMap(
                            RecipeImage::getId,
                            img -> img.getSizes() == null ? Map.of() : img.getSizes(),
                            (a, b2) -> a,
                            LinkedHashMap::new
                    ));

            b.images(imgs);
        }

        return b.build();
    }
}
