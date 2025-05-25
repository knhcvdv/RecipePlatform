package com.recipeplatform.controller;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.service.RecipeService;
import com.recipeplatform.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipe", description = "Recipe management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RecipeController {
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);
    private final RecipeService recipeService;
    private final CategoryService categoryService;

    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all recipes", description = "Retrieve a list of all recipes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recipes",
                content = @Content(schema = @Schema(implementation = Recipe.class)))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAllRecipes() {
        try {
            logger.info("Getting all recipes");
            List<Recipe> recipes = recipeService.getAllRecipes();
            logger.info("Found {} recipes", recipes.size());
            
            if (recipes.isEmpty()) {
                logger.warn("No recipes found in the database");
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body("[]");
            } else {
                // Convert to DTOs to avoid circular reference
                List<Map<String, Object>> recipeDTOs = recipes.stream()
                    .map(recipe -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", recipe.getId());
                        dto.put("title", recipe.getTitle());
                        dto.put("description", recipe.getDescription());
                        dto.put("ingredients", recipe.getIngredients());
                        if (recipe.getCategory() != null) {
                            Map<String, Object> categoryDto = new HashMap<>();
                            categoryDto.put("id", recipe.getCategory().getId());
                            categoryDto.put("name", recipe.getCategory().getName());
                            categoryDto.put("description", recipe.getCategory().getDescription());
                            dto.put("category", categoryDto);
                        }
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
                    
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(recipeDTOs);
            }
        } catch (Exception e) {
            logger.error("Error getting recipes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Failed to get recipes\", \"details\": \"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get recipe by ID", description = "Retrieve a recipe by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recipe",
                content = @Content(schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(
            @Parameter(description = "ID of the recipe to retrieve") @PathVariable Long id) {
        logger.debug("Getting recipe with id: {}", id);
        return recipeService.getRecipeById(id)
                .map(recipe -> {
                    logger.debug("Found recipe: {}", recipe.getTitle());
                    return ResponseEntity.ok(recipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new recipe", description = "Create a new recipe", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe created successfully",
                content = @Content(schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createRecipe(@RequestBody Recipe recipe) {
        try {
            logger.info("Creating new recipe with data: {}", recipe);
            
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

            if (!categoryService.getCategoryById(recipe.getCategory().getId()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Category not found with id: " + recipe.getCategory().getId());
                error.put("details", "Please create the category first or select an existing category");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Recipe savedRecipe = recipeService.createRecipe(recipe);
            logger.info("Successfully created recipe with id: {}", savedRecipe.getId());
            return ResponseEntity.ok(savedRecipe);
            
        } catch (Exception e) {
            logger.error("Error creating recipe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create recipe: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update a recipe", description = "Update an existing recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe updated successfully",
                content = @Content(schema = @Schema(implementation = Recipe.class))),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateRecipe(
            @Parameter(description = "ID of the recipe to update") @PathVariable Long id,
            @RequestBody Recipe recipeDetails) {
        try {
            logger.debug("Updating recipe with id: {}", id);
            return recipeService.updateRecipe(id, recipeDetails)
                    .map(recipe -> {
                        logger.debug("Updated recipe: {}", recipe.getTitle());
                        return ResponseEntity.ok(recipe);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error updating recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update recipe: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete a recipe", description = "Delete a recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRecipe(
            @Parameter(description = "ID of the recipe to delete") @PathVariable Long id) {
        try {
            logger.debug("Deleting recipe with id: {}", id);
            recipeService.deleteRecipe(id);
            logger.debug("Deleted recipe with id: {}", id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Error deleting recipe - not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete recipe: " + e.getMessage()));
        }
    }

    @Operation(summary = "Search recipes", description = "Search recipes by title")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recipes",
                content = @Content(schema = @Schema(implementation = Recipe.class)))
    })
    @GetMapping("/search")
    public List<Recipe> searchRecipes(
            @Parameter(description = "Title to search for") @RequestParam String title) {
        logger.debug("Searching recipes with title containing: {}", title);
        List<Recipe> recipes = recipeService.searchByTitle(title);
        logger.debug("Found {} recipes matching search criteria", recipes.size());
        return recipes;
    }
} 