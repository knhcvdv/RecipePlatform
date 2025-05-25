package com.recipeplatform.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@ToString(exclude = "category")
public class Recipe {
    private static final Logger logger = LoggerFactory.getLogger(Recipe.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "recipe_ingredients",
        joinColumns = @JoinColumn(name = "recipe_id")
    )
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    // Helper method to maintain bidirectional relationship
    public void setCategory(Category category) {
        logger.debug("Setting category for recipe. Old category: {}, New category: {}", 
            this.category != null ? this.category.getId() : "null",
            category != null ? category.getId() : "null");
            
        if (this.category != null) {
            logger.debug("Removing recipe from old category: {}", this.category.getId());
            this.category.getRecipes().remove(this);
        }
        this.category = category;
        if (category != null) {
            logger.debug("Adding recipe to new category: {}", category.getId());
            category.getRecipes().add(this);
        }
    }

    // Ensure proper initialization of ingredients list
    public void setIngredients(List<String> ingredients) {
        logger.debug("Setting ingredients for recipe. Old size: {}, New size: {}", 
            this.ingredients.size(),
            ingredients != null ? ingredients.size() : 0);
            
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
    }

    // Custom toString to prevent circular reference
    @Override
    public String toString() {
        return "Recipe(id=" + id + 
               ", title=" + title + 
               ", description=" + description + 
               ", categoryId=" + (category != null ? category.getId() : "null") + 
               ", ingredients=" + ingredients + ")";
    }

    // Validation method
    public void validate() {
        logger.debug("Validating recipe: {}", this);
        
        List<String> errors = new ArrayList<>();
        
        if (title == null || title.trim().isEmpty()) {
            errors.add("Recipe title is required");
        }
        
        if (category == null) {
            errors.add("Recipe category is required");
        } else if (category.getId() == null) {
            errors.add("Recipe category ID is required");
        }
        
        if (ingredients == null || ingredients.isEmpty()) {
            errors.add("Recipe must have at least one ingredient");
        } else {
            ingredients.forEach(ingredient -> {
                if (ingredient == null || ingredient.trim().isEmpty()) {
                    errors.add("Recipe ingredients cannot be empty");
                }
            });
        }
        
        if (!errors.isEmpty()) {
            logger.error("Recipe validation failed: {}", errors);
            throw new IllegalArgumentException("Recipe validation failed: " + String.join(", ", errors));
        }
        
        logger.debug("Recipe validation passed");
    }
} 