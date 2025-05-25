package com.recipeplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipeplatform.model.Role;
import com.recipeplatform.payload.request.LoginRequest;
import com.recipeplatform.payload.request.SignupRequest;
import com.recipeplatform.repository.RoleRepository;
import com.recipeplatform.repository.UserRepository;
import com.recipeplatform.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        initRoles();
    }

    private void initRoles() {
        if (!roleRepository.findByName(Role.ERole.ROLE_USER).isPresent()) {
            roleRepository.save(new Role(Role.ERole.ROLE_USER));
        }
        if (!roleRepository.findByName(Role.ERole.ROLE_MODERATOR).isPresent()) {
            roleRepository.save(new Role(Role.ERole.ROLE_MODERATOR));
        }
        if (!roleRepository.findByName(Role.ERole.ROLE_ADMIN).isPresent()) {
            roleRepository.save(new Role(Role.ERole.ROLE_ADMIN));
        }
    }

    @Test
    void whenSignupWithValidData_thenReturns200() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    void whenSignupWithExistingUsername_thenReturns400() throws Exception {
        // First signup
        SignupRequest firstSignup = new SignupRequest();
        firstSignup.setUsername("testuser");
        firstSignup.setEmail("test1@example.com");
        firstSignup.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstSignup)))
                .andExpect(status().isOk());

        // Second signup with same username
        SignupRequest secondSignup = new SignupRequest();
        secondSignup.setUsername("testuser");
        secondSignup.setEmail("test2@example.com");
        secondSignup.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondSignup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void whenSigninWithValidCredentials_thenReturnsJwtToken() throws Exception {
        // First create a user
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Then try to login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void whenSigninWithInvalidCredentials_thenReturns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
} 