package com.example.sharemyrecipe.spec;

import com.example.sharemyrecipe.core.enums.RecipeStatus;
import com.example.sharemyrecipe.entity.Recipe;
import com.example.sharemyrecipe.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class RecipeSpecifications {

    private RecipeSpecifications() {}

    public static Specification<Recipe> publishedBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("publishedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("publishedAt"), from);
            return cb.lessThanOrEqualTo(root.get("publishedAt"), to);
        };
    }

    public static Specification<Recipe> withStatus(RecipeStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Recipe> textSearch(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("summary")), like),
                    cb.like(cb.lower(root.get("ingredients")), like),
                    cb.like(cb.lower(root.get("steps")), like)
            );
        };
    }

    public static Specification<Recipe> byChefId(UUID chefId) {
        return (root, query, cb) -> chefId == null ? cb.conjunction()
                : cb.equal(root.get("chef").get("id"), chefId);
    }

    public static Specification<Recipe> byChefHandle(String handle) {
        return (root, query, cb) -> {
            if (handle == null || handle.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("chef").get("handle")), handle.toLowerCase());
        };
    }

    // only published
    public static Specification<Recipe> publishedOnly() {
        return (root, query, cb) -> cb.equal(root.get("status"), RecipeStatus.PUBLISHED);
    }
}
