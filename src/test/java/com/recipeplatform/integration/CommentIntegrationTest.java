package com.recipeplatform.integration;

import com.recipeplatform.model.Category;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.repository.CommentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Recipe testRecipe;
    private Category defaultCategory;

    @BeforeEach
    void setupTestData() {
        commentRepository.deleteAll();
        recipeRepository.deleteAll();
        categoryRepository.deleteAll();

        defaultCategory = Category.builder()
                .name("Основні страви")
                .description("Основні страви української кухні")
                .build();
        defaultCategory = categoryRepository.save(defaultCategory);

        testRecipe = Recipe.builder()
                .title("Борщ український")
                .description("Традиційний український борщ")
                .ingredients(Arrays.asList("буряк", "капуста", "морква"))
                .category(defaultCategory)
                .build();
        testRecipe = recipeRepository.save(testRecipe);
    }

    @Test
    @WithMockUser(username = "testUser")
    void addComment_ValidData_Success() throws Exception {
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "Дуже смачний рецепт!");

        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(commentData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Дуже смачний рецепт!"))
                .andExpect(jsonPath("$.author").value("testUser"));

        assertEquals(1, commentRepository.count());
    }

    @Test
    @WithMockUser(username = "testUser")
    void addComment_EmptyText_BadRequest() throws Exception {
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "");

        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(commentData)))
                .andExpect(status().isBadRequest());

        assertEquals(0, commentRepository.count());
    }

    @Test
    @WithMockUser(username = "testUser")
    void addComment_NonExistentRecipe_NotFound() throws Exception {
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "Коментар до неіснуючого рецепту");

        mockMvc.perform(post("/api/recipes/999/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(commentData)))
                .andExpect(status().isNotFound());

        assertEquals(0, commentRepository.count());
    }

    @Test
    void addComment_Unauthorized_Returns401() throws Exception {
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "Спроба додати коментар без авторизації");

        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(commentData)))
                .andExpect(status().isUnauthorized());

        assertEquals(0, commentRepository.count());
    }

    @Test
    @WithMockUser(username = "testUser")
    void getRecipeComments_Success() throws Exception {
        // Add a test comment first
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "Тестовий коментар");

        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(commentData)))
                .andExpect(status().isCreated());

        // Get comments
        mockMvc.perform(get("/api/recipes/" + testRecipe.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Тестовий коментар"))
                .andExpect(jsonPath("$[0].author").value("testUser"));
    }
} 