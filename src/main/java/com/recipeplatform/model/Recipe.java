package com.recipeplatform.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@ToString(exclude = "category")
public class Recipe {
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
        if (this.category != null) {
            this.category.getRecipes().remove(this);
        }
        this.category = category;
        if (category != null) {
            category.getRecipes().add(this);
        }
    }

    // Ensure proper initialization of ingredients list
    public void setIngredients(List<String> ingredients) {
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
} 