package com.recipeplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipeplatform.model.Category;
import com.recipeplatform.model.Role;
import com.recipeplatform.model.User;
import com.recipeplatform.repository.CategoryRepository;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private String moderatorToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        initRolesAndUsers();
    }

    private void initRolesAndUsers() {
        Role moderatorRole = roleRepository.findByName(Role.ERole.ROLE_MODERATOR)
                .orElseGet(() -> roleRepository.save(new Role(Role.ERole.ROLE_MODERATOR)));
        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.ERole.ROLE_ADMIN)));

        // Create moderator user
        User moderator = new User("moderator", "moderator@example.com", encoder.encode("moderator123"));
        Set<Role> moderatorRoles = new HashSet<>();
        moderatorRoles.add(moderatorRole);
        moderator.setRoles(moderatorRoles);
        userRepository.save(moderator);

        // Create admin user
        User admin = new User("admin", "admin@example.com", encoder.encode("admin123"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Generate tokens
        moderatorToken = getToken("moderator", "moderator123");
        adminToken = getToken("admin", "admin123");
    }

    private String getToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        return jwtUtils.generateJwtToken(authentication);
    }

    @Test
    void whenGetAllCategories_thenReturns200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void whenCreateCategoryAsModerator_thenReturns200() throws Exception {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");

        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + moderatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    void whenCreateCategoryWithoutAuth_thenReturns403() throws Exception {
        Category category = new Category();
        category.setName("Test Category");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUpdateCategoryAsModerator_thenReturns200() throws Exception {
        // First create a category
        Category category = new Category();
        category.setName("Original Category");
        category.setDescription("Original Description");
        Category savedCategory = categoryRepository.save(category);

        // Update the category
        category.setName("Updated Category");

        mockMvc.perform(put("/api/categories/" + savedCategory.getId())
                .header("Authorization", "Bearer " + moderatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void whenDeleteCategoryAsAdmin_thenReturns200() throws Exception {
        Category category = new Category();
        category.setName("Category to Delete");
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteCategoryAsModerator_thenReturns403() throws Exception {
        Category category = new Category();
        category.setName("Category to Delete");
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenGetCategoryById_thenReturns200() throws Exception {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(get("/api/categories/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void whenGetNonExistentCategory_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }
} 