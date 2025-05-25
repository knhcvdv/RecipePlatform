package com.recipe.platform.service.impl;

import com.recipe.platform.model.Recipe;
import com.recipe.platform.repository.RecipeRepository;
import com.recipe.platform.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class RecipeServiceImpl implements RecipeService {
    
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public Recipe createRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    @Override
    @Transactional
    public Recipe updateRecipe(Long id, Recipe recipe) {
        Recipe existingRecipe = recipeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        
        existingRecipe.setTitle(recipe.getTitle());
        existingRecipe.setDescription(recipe.getDescription());
        existingRecipe.setCategory(recipe.getCategory());
        existingRecipe.setIngredients(recipe.getIngredients());
        existingRecipe.setImageUrl(recipe.getImageUrl());
        
        return recipeRepository.save(existingRecipe);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new EntityNotFoundException("Recipe not found with id: " + id);
        }
        recipeRepository.deleteById(id);
    }

    @Override
    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
    }

    @Override
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @Override
    public List<Recipe> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Recipe> searchRecipesByIngredient(String ingredient) {
        return recipeRepository.findByIngredientName(ingredient);
    }

    @Override
    public List<Recipe> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Recipe> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorId(authorId);
    }
} 