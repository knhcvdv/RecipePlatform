package com.recipeplatform.service;

import com.recipeplatform.model.Recipe;
import java.util.List;
import java.util.Optional;

public interface RecipeService {
    List<Recipe> getAllRecipes();
    Optional<Recipe> getRecipeById(Long id);
    Recipe createRecipe(Recipe recipe);
    Optional<Recipe> updateRecipe(Long id, Recipe recipeDetails);
    void deleteRecipe(Long id);
    List<Recipe> searchByTitle(String title);
    List<Recipe> searchByIngredient(String ingredient);
    List<Recipe> searchByTitleOrDescription(String query);
} 