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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecipeListingIntegrationTest extends BaseIntegrationTest {

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
        
        Recipe recipe1 = Recipe.builder()
                .title("Томатний суп")
                .description("Легкий суп з помідорів")
                .ingredients(Arrays.asList("помідори", "цибуля", "часник"))
                .category(defaultCategory)
                .build();

        Recipe recipe2 = Recipe.builder()
                .title("Яблучний пиріг")
                .description("Солодкий пиріг з яблуками")
                .ingredients(Arrays.asList("яблука", "борошно", "цукор"))
                .category(defaultCategory)
                .build();

        recipeRepository.saveAll(Arrays.asList(recipe1, recipe2));
    }

    @Test
    @WithMockUser
    void listAllRecipes_ReturnsAllRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Томатний суп")))
                .andExpect(jsonPath("$[1].title", is("Яблучний пиріг")));
    }

    @Test
    void listAllRecipes_AsGuest_ReturnsAllRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    void searchRecipesByTitle_ReturnsMatchingRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "пиріг")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", containsString("пиріг")));
    }

    @Test
    @WithMockUser
    void searchRecipesByIngredient_ReturnsMatchingRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("ingredient", "помідори")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Томатний суп")));
    }

    @Test
    @WithMockUser
    void searchRecipes_WithNonExistentQuery_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/recipes/search")
                .param("query", "неіснуючий рецепт")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 