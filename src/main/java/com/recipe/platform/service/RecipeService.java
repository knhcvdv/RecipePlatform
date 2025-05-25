package com.recipe.platform.service;

import com.recipe.platform.model.Recipe;
import java.util.List;

public interface RecipeService {
    Recipe createRecipe(Recipe recipe);
    Recipe updateRecipe(Long id, Recipe recipe);
    void deleteRecipe(Long id);
    Recipe getRecipeById(Long id);
    List<Recipe> getAllRecipes();
    List<Recipe> searchRecipesByTitle(String title);
    List<Recipe> searchRecipesByIngredient(String ingredient);
    List<Recipe> getRecipesByCategory(Long categoryId);
    List<Recipe> getRecipesByAuthor(Long authorId);
} 