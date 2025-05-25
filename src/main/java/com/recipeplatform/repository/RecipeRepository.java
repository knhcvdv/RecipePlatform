package com.recipeplatform.repository;

import com.recipeplatform.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByNameContainingIgnoreCase(String name);
    List<Recipe> findByIngredientsContainingIgnoreCase(String ingredient);
} 