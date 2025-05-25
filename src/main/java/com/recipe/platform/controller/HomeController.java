package com.recipe.platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Recipe Platform! Available endpoints:\n" +
               "- GET /api/categories - Get all categories\n" +
               "- GET /api/categories/{id} - Get category by ID\n" +
               "- POST /api/categories - Create new category\n" +
               "- PUT /api/categories/{id} - Update category\n" +
               "- DELETE /api/categories/{id} - Delete category\n" +
               "- GET /api/recipes - Get all recipes\n" +
               "- GET /api/recipes/{id} - Get recipe by ID\n" +
               "- POST /api/recipes - Create new recipe\n" +
               "- PUT /api/recipes/{id} - Update recipe\n" +
               "- DELETE /api/recipes/{id} - Delete recipe";
    }
} 