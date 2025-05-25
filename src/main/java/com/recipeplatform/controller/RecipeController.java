package com.recipeplatform.controller;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.service.RecipeService;
import com.recipeplatform.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);
    private final RecipeService recipeService;
    private final CategoryService categoryService;

    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        logger.debug("Getting all recipes");
        List<Recipe> recipes = recipeService.getAllRecipes();
        logger.debug("Found {} recipes", recipes.size());
        return recipes;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        logger.debug("Getting recipe with id: {}", id);
        return recipeService.getRecipeById(id)
                .map(recipe -> {
                    logger.debug("Found recipe: {}", recipe.getTitle());
                    return ResponseEntity.ok(recipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody Recipe recipe) {
        try {
            logger.info("Creating new recipe with data: {}", recipe);
            
            // Validate recipe data
            if (recipe == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipe data cannot be null"));
            }
            
            if (recipe.getTitle() == null || recipe.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipe title is required"));
            }
            
            if (recipe.getCategory() == null || recipe.getCategory().getId() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipe category is required"));
            }
            
            // Log detailed information
            logger.info("Recipe title: {}", recipe.getTitle());
            logger.info("Recipe description: {}", recipe.getDescription());
            logger.info("Recipe ingredients: {}", recipe.getIngredients());
            logger.info("Recipe category ID: {}", 
                recipe.getCategory() != null ? recipe.getCategory().getId() : "null");

            // Check if category exists
            if (!categoryService.getCategoryById(recipe.getCategory().getId()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Category not found with id: " + recipe.getCategory().getId());
                error.put("details", "Please create the category first or select an existing category");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Recipe savedRecipe = recipeService.createRecipe(recipe);
            logger.info("Successfully created recipe with id: {}", savedRecipe.getId());
            return ResponseEntity.ok(savedRecipe);
            
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found error while creating recipe: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Failed to create recipe",
                    "details", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error while creating recipe: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Invalid recipe data",
                    "details", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error while creating recipe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to create recipe",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @RequestBody Recipe recipeDetails) {
        try {
            logger.debug("Updating recipe with id: {}", id);
            return recipeService.updateRecipe(id, recipeDetails)
                    .map(recipe -> {
                        logger.debug("Updated recipe: {}", recipe.getTitle());
                        return ResponseEntity.ok(recipe);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (EntityNotFoundException e) {
            logger.error("Error updating recipe - category not found: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update recipe: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        try {
            logger.debug("Deleting recipe with id: {}", id);
            recipeService.deleteRecipe(id);
            logger.debug("Deleted recipe with id: {}", id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Error deleting recipe - not found: {}", e.getMessage());
            return ResponseEntity.notFound()
                .build();
        } catch (Exception e) {
            logger.error("Error deleting recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete recipe: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public List<Recipe> searchRecipes(@RequestParam String title) {
        logger.debug("Searching recipes with title containing: {}", title);
        List<Recipe> recipes = recipeService.searchByTitle(title);
        logger.debug("Found {} recipes matching search criteria", recipes.size());
        return recipes;
    }
} 