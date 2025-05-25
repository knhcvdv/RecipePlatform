package com.recipeplatform.service;

import com.recipeplatform.model.Category;
import com.recipeplatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Desserts");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Main Course");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        // Act
        List<Category> categories = categoryService.getAllCategories();

        // Assert
        assertEquals(2, categories.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("Desserts");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> result = categoryService.getCategoryById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Desserts", result.get().getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void createCategory_ShouldReturnSavedCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Desserts");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category savedCategory = categoryService.createCategory(category);

        // Assert
        assertNotNull(savedCategory);
        assertEquals("Desserts", savedCategory.getName());
        verify(categoryRepository, times(1)).save(category);
    }
} 