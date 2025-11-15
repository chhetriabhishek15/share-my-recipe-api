package com.example.sharemyrecipe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "recipe_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeImage {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storagePath;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recipe_image_sizes", joinColumns = @JoinColumn(name = "image_id"))
    @MapKeyColumn(name = "size_label")
    @Column(name = "path")
    private Map<String, String> sizes;

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}