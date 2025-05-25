package com.recipeplatform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Schema(description = "Recipe entity representing a cooking recipe")
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
    private List<String> ingredients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    @Schema(description = "Category of the recipe")
    private Category category;

    // Constructors
    public Recipe() {}

    public Recipe(String title, String description, List<String> ingredients, Category category) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.category = category;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", categoryId=" + (category != null ? category.getId() : null) +
                '}';
    }

    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title is required");
        }
        
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Recipe category is required");
        }
        
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one ingredient");
        }
    }
} 