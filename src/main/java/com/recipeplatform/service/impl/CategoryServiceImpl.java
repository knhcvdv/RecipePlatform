package com.recipeplatform.service.impl;

import com.recipeplatform.model.Category;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, RecipeRepository recipeRepository) {
        this.categoryRepository = categoryRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        logger.info("Fetching all categories from database");
        List<Category> categories = categoryRepository.findAll();
        logger.info("Found {} categories in database", categories.size());
        categories.forEach(category -> 
            logger.debug("Category found: id={}, name={}", category.getId(), category.getName()));
        return categories;
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        logger.info("Creating new category: {}", category.getName());
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
        logger.info("Deleting category with id: {}", id);
        
        if (!categoryRepository.existsById(id)) {
            logger.warn("Category not found with id: {}", id);
            return;
        }
        
        categoryRepository.deleteById(id);
        logger.info("Successfully deleted category with id: {}", id);
    }
} 