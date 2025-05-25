package com.recipe.platform.controller;

import com.recipe.platform.model.Recipe;
import com.recipe.platform.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        Recipe createdRecipe = recipeService.createRecipe(recipe);
        return new ResponseEntity<>(createdRecipe, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, @RequestBody Recipe recipe) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> searchRecipes(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String ingredient) {
        if (title != null) {
            return ResponseEntity.ok(recipeService.searchRecipesByTitle(title));
        } else if (ingredient != null) {
            return ResponseEntity.ok(recipeService.searchRecipesByIngredient(ingredient));
        }
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Recipe>> getRecipesByCategory(@PathVariable Long categoryId) {
        List<Recipe> recipes = recipeService.getRecipesByCategory(categoryId);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Recipe>> getRecipesByAuthor(@PathVariable Long authorId) {
        List<Recipe> recipes = recipeService.getRecipesByAuthor(authorId);
        return ResponseEntity.ok(recipes);
    }
} 