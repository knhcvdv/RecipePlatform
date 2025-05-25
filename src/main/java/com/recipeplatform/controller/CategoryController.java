package com.recipeplatform.controller;

import com.recipeplatform.model.Category;
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
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "Category management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all categories", description = "Retrieve a list of all categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories",
                content = @Content(schema = @Schema(implementation = Category.class)))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.info("Getting all categories");
            List<Category> categories = categoryService.getAllCategories();
            logger.info("Found {} categories", categories.size());
            
            if (categories.isEmpty()) {
                logger.warn("No categories found in the database");
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body("[]");
            } else {
                // Convert to DTOs to avoid circular reference
                List<Map<String, Object>> categoryDTOs = categories.stream()
                    .map(category -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", category.getId());
                        dto.put("name", category.getName());
                        dto.put("description", category.getDescription());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
                    
                categories.forEach(category -> 
                    logger.info("Category: id={}, name={}, description={}", 
                        category.getId(), 
                        category.getName(),
                        category.getDescription()));
                        
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(categoryDTOs);
            }
        } catch (Exception e) {
            logger.error("Error getting categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Failed to get categories\", \"details\": \"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved category",
                content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable Long id) {
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

    @Operation(summary = "Create a new category", description = "Create a new category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category created successfully",
                content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
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

    @Operation(summary = "Update a category", description = "Update an existing category by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
                content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable Long id,
            @RequestBody Category categoryDetails) {
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

    @Operation(summary = "Delete a category", description = "Delete a category by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long id) {
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
                .body(Map.of("message", "Category and all associated recipes deleted successfully"));
                
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