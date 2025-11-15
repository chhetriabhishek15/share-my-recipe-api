package com.example.sharemyrecipe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.sharemyrecipe.dto.RecipeCreateDto;
import com.example.sharemyrecipe.dto.RecipeListResponse;
import com.example.sharemyrecipe.dto.RecipeResponseDto;
import com.example.sharemyrecipe.core.enums.RecipeStatus;
import com.example.sharemyrecipe.mapper.RecipeMapper;
import com.example.sharemyrecipe.entity.Recipe;
import com.example.sharemyrecipe.entity.RecipeImage;
import com.example.sharemyrecipe.entity.User;
import com.example.sharemyrecipe.repository.RecipeImageRepository;
import com.example.sharemyrecipe.repository.RecipeRepository;
import com.example.sharemyrecipe.repository.UserRepository;
import com.example.sharemyrecipe.service.RecipeService;
import com.example.sharemyrecipe.spec.RecipeSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeImageRepository recipeImageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Queue imageResizeQueue;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.base-path:uploads}")
    private String uploadBasePath;

    @Override
    @Transactional
    public RecipeResponseDto createRecipe(UUID chefId, RecipeCreateDto dto, MultipartFile[] images) {
        log.debug("Creating recipe for chefId={} title={}", chefId, dto.getTitle());
        User chef = userRepository.findById(chefId).orElseThrow(() -> new IllegalArgumentException("chef not found"));

        Recipe recipe = Recipe.builder()
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .ingredients(dto.getIngredients())
                .steps(dto.getSteps())
                .labels(dto.getLabels() == null ? Set.of() : dto.getLabels())
                .status(dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("PUBLISHED") ? RecipeStatus.PUBLISHED : RecipeStatus.DRAFT)
                .publishedAt(dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("PUBLISHED") ? Instant.now() : null)
                .chef(chef)
                .build();

        Recipe saved = recipeRepository.save(recipe);

        // handle images: save original files and create RecipeImage rows, then publish resize tasks
        if (images != null && images.length > 0) {
            String instanceDir = uploadBasePath + File.separator + "recipes" + File.separator + saved.getId();
            Path instancePath = Path.of(instanceDir);
            try {
                Files.createDirectories(instancePath);
            } catch (Exception e) {
                log.error("Failed to create upload dir: {}", instancePath, e);
            }

            for (MultipartFile mf : images) {
                if (mf == null || mf.isEmpty()) continue;
                String originalFilename = Objects.requireNonNull(mf.getOriginalFilename());
                String storedName = UUID.randomUUID().toString() + "_" + originalFilename;
                Path target = instancePath.resolve(storedName);
                try {
                    Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    log.error("Failed to save uploaded file {}", originalFilename, e);
                    continue;
                }

                RecipeImage ri = RecipeImage.builder()
                        .originalFilename(originalFilename)
                        .storagePath(instanceDir + File.separator + storedName)
                        .recipe(saved)
                        .build();

                RecipeImage savedImg = recipeImageRepository.save(ri);
                // attach to recipe (in-memory)
                saved.getImages().add(savedImg);

                // publish message to RabbitMQ for worker processing
                Map<String, Object> payload = new HashMap<>();
                payload.put("imageId", savedImg.getId().toString());
                payload.put("path", savedImg.getStoragePath());
                payload.put("recipeId", saved.getId().toString());
                // sizes recommended: thumb(200x200), medium(800x600), large(1600x1200)
                payload.put("sizes", List.of(Map.of("label", "thumb", "w", 200, "h", 200),
                        Map.of("label", "medium", "w", 800, "h", 600),
                        Map.of("label", "large", "w", 1600, "h", 1200)));
                try {
                    String json = objectMapper.writeValueAsString(payload);
                    rabbitTemplate.convertAndSend(imageResizeQueue.getName(), json);
                    log.info("Published image resize task for imageId={} path={}", savedImg.getId(), savedImg.getStoragePath());
                } catch (Exception ex) {
                    log.error("Failed to publish resize task to rabbit", ex);
                }
            }
        }

        return recipeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RecipeResponseDto updateRecipe(UUID chefId, UUID recipeId, RecipeCreateDto dto) {
        log.debug("Update recipe id={} chefId={}", recipeId, chefId);
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new IllegalArgumentException("recipe not found"));
        if (!recipe.getChef().getId().equals(chefId)) {
            throw new SecurityException("not allowed");
        }
        recipe.setTitle(dto.getTitle() == null ? recipe.getTitle() : dto.getTitle());
        recipe.setSummary(dto.getSummary() == null ? recipe.getSummary() : dto.getSummary());
        recipe.setIngredients(dto.getIngredients() == null ? recipe.getIngredients() : dto.getIngredients());
        recipe.setSteps(dto.getSteps() == null ? recipe.getSteps() : dto.getSteps());
        if (dto.getLabels() != null) recipe.setLabels(dto.getLabels());
        if (dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("PUBLISHED")) {
            recipe.setStatus(RecipeStatus.PUBLISHED);
            recipe.setPublishedAt(Instant.now());
        }
        recipe = recipeRepository.save(recipe);
        return recipeMapper.toDto(recipe);
    }

    @Override
    @Transactional
    public void deleteRecipe(UUID chefId, UUID recipeId) {
        log.debug("Delete recipe id={} chefId={}", recipeId, chefId);
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new IllegalArgumentException("recipe not found"));
        if (!recipe.getChef().getId().equals(chefId)) {
            throw new SecurityException("not allowed");
        }
        // soft delete would be better; for now mark DELETED
        recipe.setStatus(RecipeStatus.DELETED);
        recipeRepository.save(recipe);
    }

    @Override
    public RecipeResponseDto getRecipe(UUID id, UUID requestingUserId) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("recipe not found"));
        // allow if published or owner or admin (skip admin check for brevity)
        if (recipe.getStatus() != RecipeStatus.PUBLISHED && !recipe.getChef().getId().equals(requestingUserId)) {
            throw new SecurityException("not allowed to view recipe");
        }
        return recipeMapper.toDto(recipe);
    }

    @Override
    public RecipeListResponse<RecipeResponseDto> listPublicRecipes(String q, Instant publishedFrom, Instant publishedTo, UUID chefId, String chefHandle, Pageable pageable) {
        Specification<Recipe> spec = Specification.where(RecipeSpecifications.publishedOnly())
                .and(RecipeSpecifications.textSearch(q))
                .and(RecipeSpecifications.publishedBetween(publishedFrom, publishedTo))
                .and(RecipeSpecifications.byChefId(chefId))
                .and(RecipeSpecifications.byChefHandle(chefHandle));

        Page<Recipe> page = recipeRepository.findAll(spec, pageable);
        List<RecipeResponseDto> dtos = page.map(recipeMapper::toDto).getContent();

        RecipeListResponse.PaginationMeta meta = RecipeListResponse.PaginationMeta.builder()
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return RecipeListResponse.<RecipeResponseDto>builder()
                .data(dtos)
                .meta(meta)
                .build();
    }
}
