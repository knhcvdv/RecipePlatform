package com.recipeplatform.service;

import com.recipeplatform.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category createCategory(Category category);
    Optional<Category> updateCategory(Long id, Category categoryDetails);
    void deleteCategory(Long id);
} 