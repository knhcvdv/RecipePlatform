package com.recipeplatform.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
@Schema(description = "Category entity representing a recipe category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the category", example = "1")
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the category", example = "Main Dishes", required = true)
    private String name;

    @Size(max = 200)
    @Column(name = "description")
    @Schema(description = "Description of the category", example = "Main course dishes")
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "List of recipes in this category")
    @Builder.Default
    private List<Recipe> recipes = new ArrayList<>();

    public void addRecipe(Recipe recipe) {
        if (recipes == null) {
            recipes = new ArrayList<>();
        }
        recipes.add(recipe);
        if (recipe.getCategory() != this) {
            recipe.setCategory(this);
        }
    }
    
    public void removeRecipe(Recipe recipe) {
        if (recipes != null) {
            recipes.remove(recipe);
            if (recipe.getCategory() == this) {
                recipe.setCategory(null);
            }
        }
    }
} 