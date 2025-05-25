package com.recipe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.recipe.platform.model")
@EnableJpaRepositories("com.recipe.platform.repository")
public class RecipePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecipePlatformApplication.class, args);
    }
} 