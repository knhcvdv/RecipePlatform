package com.recipeplatform.service;

import com.recipeplatform.model.Category;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act
        Optional<Category> result = categoryService.getCategoryById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCategory.getName(), result.get().getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void createCategory_ShouldReturnSavedCategory() {
        // Arrange
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        Category result = categoryService.createCategory(testCategory);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getName(), result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenExists_ShouldReturnUpdatedCategory() {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Name");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Act
        Optional<Category> result = categoryService.updateCategory(1L, updatedCategory);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(updatedCategory.getName(), result.get().getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldCallRepository() {
        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository).deleteById(1L);
    }
} 