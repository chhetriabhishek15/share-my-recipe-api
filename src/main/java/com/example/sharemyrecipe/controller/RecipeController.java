package com.example.sharemyrecipe.controller;
import com.example.sharemyrecipe.dto.RecipeCreateDto;
import com.example.sharemyrecipe.dto.RecipeListResponse;
import com.example.sharemyrecipe.dto.RecipeResponseDto;
import com.example.sharemyrecipe.entity.User;
import com.example.sharemyrecipe.service.RecipeService;
import com.example.sharemyrecipe.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<RecipeListResponse<RecipeResponseDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant published_from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant published_to,
            @RequestParam(required = false) UUID chef_id,
            @RequestParam(required = false) String chef_handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, pageSize));
        var resp = recipeService.listPublicRecipes(q, published_from, published_to, chef_id, chef_handle, pageable);
        return ResponseEntity.ok(resp);
    }

    // create recipe + images: multipart (json part + files)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponseDto> create(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Principal principal
    ) {
        RecipeCreateDto dto;
        try {
            dto = new ObjectMapper().readValue(dataJson, RecipeCreateDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON for 'data' field", e);
        }

        User current = userService.findByEmail(principal.getName()).orElseThrow();
        RecipeResponseDto created = recipeService.createRecipe(current.getId(), dto, images);

        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> get(@PathVariable UUID id, Principal principal) {
        UUID reqUserId = null;
        if (principal != null) {
            var userOpt = userService.findByEmail(principal.getName());
            if (userOpt.isPresent()) reqUserId = userOpt.get().getId();
        }
        var dto = recipeService.getRecipe(id, reqUserId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> update(@PathVariable UUID id, @RequestBody RecipeCreateDto dto, Principal principal) {
        var current = userService.findByEmail(principal.getName()).orElseThrow();
        var updated = recipeService.updateRecipe(current.getId(), id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        var current = userService.findByEmail(principal.getName()).orElseThrow();
        recipeService.deleteRecipe(current.getId(), id);
        return ResponseEntity.noContent().build();
    }
}