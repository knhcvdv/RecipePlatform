package com.recipeplatform.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@ToString(exclude = "recipes")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Recipe> recipes = new ArrayList<>();
    
    // Helper method to maintain bidirectional relationship
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