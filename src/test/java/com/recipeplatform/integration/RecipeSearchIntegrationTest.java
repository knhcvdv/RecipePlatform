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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecipeSearchIntegrationTest extends BaseIntegrationTest {

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

        // Create test recipes
        Recipe recipe1 = Recipe.builder()
                .title("Борщ український")
                .description("Традиційний український борщ")
                .ingredients(Arrays.asList("буряк", "капуста", "морква", "картопля"))
                .category(defaultCategory)
                .build();

        Recipe recipe2 = Recipe.builder()
                .title("Вареники з картоплею")
                .description("Вареники з картопляною начинкою")
                .ingredients(Arrays.asList("борошно", "картопля", "цибуля"))
                .category(defaultCategory)
                .build();

        Recipe recipe3 = Recipe.builder()
                .title("Деруни")
                .description("Картопляні деруни зі сметаною")
                .ingredients(Arrays.asList("картопля", "цибуля", "яйця", "борошно"))
                .category(defaultCategory)
                .build();

        recipeRepository.saveAll(Arrays.asList(recipe1, recipe2, recipe3));
    }

    @Test
    @WithMockUser
    void searchByTitle_ExactMatch() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "Борщ український")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Борщ український"));
    }

    @Test
    @WithMockUser
    void searchByTitle_PartialMatch() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "вареники")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Вареники з картоплею"));
    }

    @Test
    @WithMockUser
    void searchByIngredient_SingleIngredient() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("ingredient", "буряк")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Борщ український"));
    }

    @Test
    @WithMockUser
    void searchByIngredient_CommonIngredient() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("ingredient", "картопля")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser
    void searchByDescription_PartialMatch() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "традиційний український")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Борщ український"));
    }

    @Test
    @WithMockUser
    void searchWithNoResults() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "неіснуючий рецепт")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchAsGuest_Success() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "борщ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
} 