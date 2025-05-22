package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;

@Controller
@RequestMapping("/supermarket")
public class SupermarketController {

    /**
     * Display the supermarket home page showing products by category
     */
    @GetMapping("/home")
    public String showSupermarketHome(Model model, Principal principal, HttpSession session) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "supermarket-home";
    }

    /**
     * Display the supermarket orders page
     */
    @GetMapping("/orders")
    public String showSupermarketOrders(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "supermarket-orders";
    }

    /**
     * Display the shopping basket page
     */
    @GetMapping("/basket")
    public String showShoppingBasket(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "supermarket-basket";
    }
}
