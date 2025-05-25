package com.recipeplatform.integration;

import com.recipeplatform.model.Category;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecipeCreationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category defaultCategory;

    @BeforeEach
    void setupTestData() {
        recipeRepository.deleteAll();
        categoryRepository.deleteAll();
        
        defaultCategory = Category.builder()
                .name("Основні страви")
                .description("Основні страви української кухні")
                .build();
        defaultCategory = categoryRepository.save(defaultCategory);
    }

    @Test
    @WithMockUser
    void createRecipe_ValidData_Success() throws Exception {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", "Борщ український");
        recipeData.put("description", "Традиційний український борщ");
        recipeData.put("ingredients", Arrays.asList("буряк", "капуста", "морква", "цибуля"));
        recipeData.put("categoryId", defaultCategory.getId());

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(recipeData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Борщ український"))
                .andExpect(jsonPath("$.ingredients").isArray())
                .andExpect(jsonPath("$.ingredients.length()").value(4));

        assertEquals(1, recipeRepository.count());
    }

    @Test
    @WithMockUser
    void createRecipe_NoTitle_BadRequest() throws Exception {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("description", "Опис без назви");
        recipeData.put("ingredients", Arrays.asList("інгредієнт1", "інгредієнт2"));
        recipeData.put("categoryId", defaultCategory.getId());

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(recipeData)))
                .andExpect(status().isBadRequest());

        assertEquals(0, recipeRepository.count());
    }

    @Test
    @WithMockUser
    void createRecipe_NoIngredients_BadRequest() throws Exception {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", "Рецепт без інгредієнтів");
        recipeData.put("description", "Опис рецепту");
        recipeData.put("ingredients", Arrays.asList());
        recipeData.put("categoryId", defaultCategory.getId());

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(recipeData)))
                .andExpect(status().isBadRequest());

        assertEquals(0, recipeRepository.count());
    }

    @Test
    @WithMockUser
    void createRecipe_InvalidCategoryId_BadRequest() throws Exception {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", "Рецепт з неправильною категорією");
        recipeData.put("description", "Опис рецепту");
        recipeData.put("ingredients", Arrays.asList("інгредієнт1", "інгредієнт2"));
        recipeData.put("categoryId", 999L); // Non-existent category ID

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(recipeData)))
                .andExpect(status().isBadRequest());

        assertEquals(0, recipeRepository.count());
    }

    @Test
    void createRecipe_Unauthorized_Returns401() throws Exception {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", "Тестовий рецепт");
        recipeData.put("description", "Опис рецепту");
        recipeData.put("ingredients", Arrays.asList("інгредієнт1", "інгредієнт2"));
        recipeData.put("categoryId", defaultCategory.getId());

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(recipeData)))
                .andExpect(status().isUnauthorized());

        assertEquals(0, recipeRepository.count());
    }
} 