package com.recipeplatform.controller;

import com.recipeplatform.model.Category;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.info("Getting all categories");
            List<Category> categories = categoryService.getAllCategories();
            logger.info("Found {} categories", categories.size());
            
            if (categories.isEmpty()) {
                logger.warn("No categories found in the database");
                Map<String, String> response = new HashMap<>();
                response.put("message", "No categories found");
                response.put("hint", "Please create a category first before creating recipes");
                return ResponseEntity.ok(response);
            } else {
                categories.forEach(category -> 
                    logger.info("Category: id={}, name={}", category.getId(), category.getName()));
                return ResponseEntity.ok(categories);
            }
        } catch (Exception e) {
            logger.error("Error getting categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to get categories",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            logger.info("Getting category with id: {}", id);
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                logger.info("Found category: {}", category.getName());
                return ResponseEntity.ok(category);
            } else {
                logger.warn("Category not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "Category not found",
                        "id", id.toString()
                    ));
            }
        } catch (Exception e) {
            logger.error("Error getting category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to get category",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            logger.info("Creating new category: {}", category);
            
            // Validate category
            if (category == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category data cannot be null"));
            }
            
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category name is required"));
            }
            
            // Trim the name
            category.setName(category.getName().trim());
            
            Category savedCategory = categoryService.createCategory(category);
            logger.info("Created category with id: {}", savedCategory.getId());
            return ResponseEntity.ok(savedCategory);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error creating category - validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Invalid category data",
                    "details", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Error creating category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to create category",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        try {
            logger.debug("Updating category with id: {}", id);
            return categoryService.updateCategory(id, categoryDetails)
                    .map(category -> {
                        logger.debug("Updated category: {}", category.getName());
                        return ResponseEntity.ok(category);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error updating category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            logger.info("Deleting category with id: {}", id);
            
            // Check if category exists
            if (!categoryService.getCategoryById(id).isPresent()) {
                logger.warn("Category not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "Category not found",
                        "id", id.toString()
                    ));
            }
            
            categoryService.deleteCategory(id);
            logger.info("Successfully deleted category with id: {}", id);
            return ResponseEntity.ok()
                .body(Map.of("message", "Category deleted successfully"));
                
        } catch (EntityNotFoundException e) {
            logger.error("Error deleting category - not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", "Category not found",
                    "details", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Error deleting category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to delete category",
                    "details", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }
} 