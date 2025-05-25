package com.recipeplatform.service.impl;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Category;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeServiceImpl implements RecipeService {
    private static final Logger logger = LoggerFactory.getLogger(RecipeServiceImpl.class);
    
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    @Override
    @Transactional
    public Recipe createRecipe(Recipe recipe) {
        logger.info("Creating recipe: {}", recipe);
        
        try {
            // Validate recipe
            recipe.validate();
            
            // Load the category
            Category category = categoryRepository.findById(recipe.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + recipe.getCategory().getId()));
            
            // Create a new recipe instance to avoid any potential attached entities
            Recipe newRecipe = new Recipe();
            newRecipe.setTitle(recipe.getTitle().trim());
            newRecipe.setDescription(recipe.getDescription() != null ? recipe.getDescription().trim() : null);
            newRecipe.setIngredients(recipe.getIngredients());
            
            // Set up the relationship
            category.addRecipe(newRecipe);
            
            // Save the recipe
            Recipe savedRecipe = recipeRepository.save(newRecipe);
            logger.info("Created recipe with ID: {}", savedRecipe.getId());
            return savedRecipe;
            
        } catch (EntityNotFoundException e) {
            logger.error("Category not found: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating recipe", e);
            throw new RuntimeException("Failed to create recipe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Optional<Recipe> updateRecipe(Long id, Recipe recipeDetails) {
        logger.info("Updating recipe with ID {}: {}", id, recipeDetails);
        
        try {
            // Validate the update data
            recipeDetails.validate();
            
            return recipeRepository.findById(id)
                .map(recipe -> {
                    // Update category if changed
                    if (recipeDetails.getCategory() != null && 
                        !recipe.getCategory().getId().equals(recipeDetails.getCategory().getId())) {
                        
                        Category newCategory = categoryRepository.findById(recipeDetails.getCategory().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + 
                                recipeDetails.getCategory().getId()));
                        
                        recipe.getCategory().removeRecipe(recipe);
                        newCategory.addRecipe(recipe);
                    }
                    
                    recipe.setTitle(recipeDetails.getTitle().trim());
                    recipe.setDescription(recipeDetails.getDescription() != null ? 
                        recipeDetails.getDescription().trim() : null);
                    recipe.setIngredients(recipeDetails.getIngredients());
                    
                    Recipe updatedRecipe = recipeRepository.save(recipe);
                    logger.info("Updated recipe: {}", updatedRecipe);
                    return updatedRecipe;
                });
        } catch (Exception e) {
            logger.error("Error updating recipe", e);
            throw new RuntimeException("Failed to update recipe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        logger.info("Deleting recipe with ID: {}", id);
        recipeRepository.findById(id).ifPresent(recipe -> {
            try {
                recipe.getCategory().removeRecipe(recipe);
                recipeRepository.delete(recipe);
                logger.info("Deleted recipe with ID: {}", id);
            } catch (Exception e) {
                logger.error("Error deleting recipe", e);
                throw new RuntimeException("Failed to delete recipe: " + e.getMessage(), e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recipe> searchByTitle(String title) {
        logger.info("Searching recipes by title: {}", title);
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        logger.info("Found {} recipes", recipes.size());
        return recipes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recipe> searchByIngredient(String ingredient) {
        logger.info("Searching recipes by ingredient: {}", ingredient);
        List<Recipe> recipes = recipeRepository.findByIngredientsContainingIgnoreCase(ingredient);
        logger.info("Found {} recipes", recipes.size());
        return recipes;
    }
} 