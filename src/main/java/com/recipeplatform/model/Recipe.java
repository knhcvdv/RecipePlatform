package com.recipeplatform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Schema(description = "Recipe entity representing a cooking recipe")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the recipe", example = "1")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    @Schema(description = "Title of the recipe", example = "Spaghetti Carbonara", required = true)
    private String title;

    @Size(max = 1000)
    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Description of the recipe", example = "A classic Italian pasta dish")
    private String description;

    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient")
    @Schema(description = "List of ingredients for the recipe")
    @Builder.Default
    private List<String> ingredients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    @Schema(description = "Category of the recipe")
    private Category category;

    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title is required");
        }
        
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Recipe category is required");
        }
        
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
    }
} 