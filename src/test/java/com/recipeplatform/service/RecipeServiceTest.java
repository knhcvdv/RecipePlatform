package com.recipeplatform.service;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Category;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.service.impl.RecipeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.persistence.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecipeService recipeService;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeServiceImpl(recipeRepository);
        
        // Setup test recipe
        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setDescription("Test Description");
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        testRecipe.setCategory(category);
    }

    @Test
    void createRecipe_ShouldReturnSavedRecipe() {
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        Recipe savedRecipe = recipeService.createRecipe(testRecipe);

        assertNotNull(savedRecipe);
        assertEquals(testRecipe.getTitle(), savedRecipe.getTitle());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void getRecipeById_ShouldReturnRecipe() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        Optional<Recipe> foundRecipe = recipeService.getRecipeById(1L);

        assertTrue(foundRecipe.isPresent());
        assertEquals(testRecipe.getId(), foundRecipe.get().getId());
        verify(recipeRepository).findById(1L);
    }

    @Test
    void getRecipeById_ShouldThrowException_WhenRecipeNotFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Recipe> result = recipeService.getRecipeById(1L);
        assertTrue(result.isEmpty());
        verify(recipeRepository).findById(1L);
    }

    @Test
    void getAllRecipes_ShouldReturnListOfRecipes() {
        List<Recipe> recipes = Arrays.asList(testRecipe);
        when(recipeRepository.findAll()).thenReturn(recipes);

        List<Recipe> foundRecipes = recipeService.getAllRecipes();

        assertNotNull(foundRecipes);
        assertFalse(foundRecipes.isEmpty());
        assertEquals(1, foundRecipes.size());
        verify(recipeRepository).findAll();
    }

    @Test
    void searchRecipesByTitle_ShouldReturnMatchingRecipes() {
        String searchTitle = "Test";
        List<Recipe> recipes = Arrays.asList(testRecipe);
        when(recipeRepository.findByTitleContainingIgnoreCase(searchTitle)).thenReturn(recipes);

        List<Recipe> foundRecipes = recipeService.searchByTitle(searchTitle);

        assertNotNull(foundRecipes);
        assertFalse(foundRecipes.isEmpty());
        assertEquals(1, foundRecipes.size());
        verify(recipeRepository).findByTitleContainingIgnoreCase(searchTitle);
    }

    @Test
    void deleteRecipe_ShouldDeleteExistingRecipe() {
        when(recipeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(recipeRepository).deleteById(1L);

        assertDoesNotThrow(() -> recipeService.deleteRecipe(1L));
        verify(recipeRepository).deleteById(1L);
    }

    @Test
    void deleteRecipe_ShouldThrowException_WhenRecipeNotFound() {
        when(recipeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> recipeService.deleteRecipe(1L));
        verify(recipeRepository, never()).deleteById(1L);
    }
} 