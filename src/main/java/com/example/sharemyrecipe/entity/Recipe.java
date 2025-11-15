package com.example.sharemyrecipe.entity;

import com.example.sharemyrecipe.core.enums.RecipeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "recipes",
        indexes = {
                @Index(name = "idx_recipe_title", columnList = "title"),
                @Index(name = "idx_recipe_publishedat", columnList = "publishedAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Lob
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients; // JSON or newline separated list

    @Lob
    @Column(name = "steps", columnDefinition = "TEXT")
    private String steps; // JSON or newline separated list

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recipe_labels", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "label")
    private Set<String> labels = Set.of();

    @Enumerated(EnumType.STRING)
    private RecipeStatus status = RecipeStatus.DRAFT;

    private Instant publishedAt;

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chef_id")
    private User chef;

    @OneToMany(mappedBy = "recipe", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RecipeImage> images = new LinkedHashSet<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
