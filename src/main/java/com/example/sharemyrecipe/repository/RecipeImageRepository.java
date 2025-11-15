package com.example.sharemyrecipe.repository;

import com.example.sharemyrecipe.entity.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeImageRepository extends JpaRepository<RecipeImage, UUID> {
}
