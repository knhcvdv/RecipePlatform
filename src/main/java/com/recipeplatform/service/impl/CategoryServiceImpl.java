package com.recipeplatform.service.impl;

import com.recipeplatform.model.Category;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, RecipeRepository recipeRepository) {
        this.categoryRepository = categoryRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Optional<Category> updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id)
            .map(category -> {
                category.setName(categoryDetails.getName());
                category.setDescription(categoryDetails.getDescription());
                return categoryRepository.save(category);
            });
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // First delete all recipes in this category
        recipeRepository.deleteAllByCategoryId(id);
        // Then delete the category
        categoryRepository.deleteById(id);
    }
} 