package com.recipeplatform.config;

import com.recipeplatform.model.Role;
import com.recipeplatform.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        try {
            for (Role.ERole roleEnum : Role.ERole.values()) {
                if (!roleRepository.existsByName(roleEnum)) {
                    Role role = new Role(roleEnum);
                    roleRepository.save(role);
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing roles: " + e.getMessage());
        }
    }
} 