package com.recipeplatform.service;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Category;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.service.impl.RecipeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.persistence.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private Recipe testRecipe;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setDescription("Test Description");
        testRecipe.setCategory(testCategory);
        testRecipe.setIngredients(Arrays.asList("ingredient1", "ingredient2"));
    }

    @Test
    void getAllRecipes_ShouldReturnList() {
        // Arrange
        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        // Act
        List<Recipe> result = recipeService.getAllRecipes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipeRepository).findAll();
    }

    @Test
    void getRecipeById_WhenExists_ShouldReturnRecipe() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // Act
        Optional<Recipe> result = recipeService.getRecipeById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRecipe.getTitle(), result.get().getTitle());
        verify(recipeRepository).findById(1L);
    }

    @Test
    void createRecipe_ShouldReturnSavedRecipe() {
        // Arrange
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // Act
        Recipe result = recipeService.createRecipe(testRecipe);

        // Assert
        assertNotNull(result);
        assertEquals(testRecipe.getTitle(), result.getTitle());
        assertEquals(testRecipe.getCategory().getName(), result.getCategory().getName());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void updateRecipe_WhenExists_ShouldReturnUpdatedRecipe() {
        // Arrange
        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setTitle("Updated Title");
        updatedRecipe.setDescription("Updated Description");
        updatedRecipe.setCategory(testCategory);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(updatedRecipe);

        // Act
        Optional<Recipe> result = recipeService.updateRecipe(1L, updatedRecipe);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(updatedRecipe.getTitle(), result.get().getTitle());
        verify(recipeRepository).findById(1L);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void deleteRecipe_ShouldCallRepository() {
        // Act
        recipeService.deleteRecipe(1L);

        // Assert
        verify(recipeRepository).deleteById(1L);
    }

    @Test
    void searchByTitle_ShouldReturnMatchingRecipes() {
        // Arrange
        String searchTitle = "Test";
        when(recipeRepository.findByTitleContainingIgnoreCase(searchTitle))
            .thenReturn(Arrays.asList(testRecipe));

        // Act
        List<Recipe> result = recipeService.searchByTitle(searchTitle);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipe.getTitle(), result.get(0).getTitle());
        verify(recipeRepository).findByTitleContainingIgnoreCase(searchTitle);
    }

    @Test
    void getRecipeById_ShouldThrowException_WhenRecipeNotFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Recipe> result = recipeService.getRecipeById(1L);
        assertTrue(result.isEmpty());
        verify(recipeRepository).findById(1L);
    }

    @Test
    void deleteRecipe_ShouldThrowException_WhenRecipeNotFound() {
        when(recipeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> recipeService.deleteRecipe(1L));
        verify(recipeRepository, never()).deleteById(1L);
    }
} 