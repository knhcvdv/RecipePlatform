package com.recipeplatform.repository;

import com.recipeplatform.model.Category;
import com.recipeplatform.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByTitleContainingIgnoreCase(String title);
    List<Recipe> findByIngredientsContainingIgnoreCase(String ingredient);
    List<Recipe> findByCategoryId(Long categoryId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Recipe r WHERE r.category = :category")
    void deleteByCategory(@Param("category") Category category);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Recipe r WHERE r.category.id = :categoryId")
    void deleteAllByCategoryId(@Param("categoryId") Long categoryId);
} 