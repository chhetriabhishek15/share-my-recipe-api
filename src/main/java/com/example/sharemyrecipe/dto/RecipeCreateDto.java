package com.example.sharemyrecipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RecipeCreateDto {
    @NotBlank
    private String title;

    private String summary;

    private String ingredients;

    private String steps;

    private Set<String> labels;

    private String status;
}