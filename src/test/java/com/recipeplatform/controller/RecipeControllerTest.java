package com.recipeplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipeplatform.model.Category;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Role;
import com.recipeplatform.model.User;
import com.recipeplatform.repository.CategoryRepository;
import com.recipeplatform.repository.RecipeRepository;
import com.recipeplatform.repository.RoleRepository;
import com.recipeplatform.repository.UserRepository;
import com.recipeplatform.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        initRolesAndUsers();
        createTestCategory();
    }

    private void initRolesAndUsers() {
        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(Role.ERole.ROLE_USER)));
        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.ERole.ROLE_ADMIN)));

        // Create test user
        User user = new User("testuser", "test@example.com", encoder.encode("password123"));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        user.setRoles(userRoles);
        userRepository.save(user);

        // Create admin user
        User admin = new User("admin", "admin@example.com", encoder.encode("admin123"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Generate tokens
        userToken = getToken("testuser", "password123");
        adminToken = getToken("admin", "admin123");
    }

    private String getToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        return jwtUtils.generateJwtToken(authentication);
    }

    private void createTestCategory() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        testCategory = categoryRepository.save(category);
    }

    @Test
    void whenGetAllRecipes_thenReturns200() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk());
    }

    @Test
    void whenCreateRecipeAsUser_thenReturns200() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        recipe.setIngredients(List.of("Ingredient 1", "Ingredient 2"));
        recipe.setCategory(testCategory);

        mockMvc.perform(post("/api/recipes")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Recipe"));
    }

    @Test
    void whenCreateRecipeWithoutAuth_thenReturns403() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setCategory(testCategory);

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUpdateRecipeAsAdmin_thenReturns200() throws Exception {
        // First create a recipe
        Recipe recipe = new Recipe();
        recipe.setTitle("Original Recipe");
        recipe.setDescription("Original Description");
        recipe.setCategory(testCategory);
        Recipe savedRecipe = recipeRepository.save(recipe);

        // Update the recipe
        recipe.setTitle("Updated Recipe");

        mockMvc.perform(put("/api/recipes/" + savedRecipe.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Recipe"));
    }

    @Test
    void whenDeleteRecipeAsAdmin_thenReturns200() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Recipe to Delete");
        recipe.setCategory(testCategory);
        Recipe savedRecipe = recipeRepository.save(recipe);

        mockMvc.perform(delete("/api/recipes/" + savedRecipe.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteRecipeAsUser_thenReturns403() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Recipe to Delete");
        recipe.setCategory(testCategory);
        Recipe savedRecipe = recipeRepository.save(recipe);

        mockMvc.perform(delete("/api/recipes/" + savedRecipe.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenSearchRecipes_thenReturns200() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Searchable Recipe");
        recipe.setCategory(testCategory);
        recipeRepository.save(recipe);

        mockMvc.perform(get("/api/recipes/search")
                .param("title", "Searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Searchable Recipe"));
    }
} 