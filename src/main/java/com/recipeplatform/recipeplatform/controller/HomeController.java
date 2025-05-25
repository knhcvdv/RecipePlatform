package com.recipeplatform.recipeplatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // Обробка запиту до кореневої сторінки
    @GetMapping("/")
    public String home() {
        return "Welcome to the Recipe Platform!";
    }
}
