package com.recipe.platform.repository;

import com.recipe.platform.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByTitleContainingIgnoreCase(String title);
    
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE i.name LIKE %:ingredient%")
    List<Recipe> findByIngredientName(@Param("ingredient") String ingredient);
    
    List<Recipe> findByCategoryId(Long categoryId);
    
    List<Recipe> findByAuthorId(Long authorId);
} 