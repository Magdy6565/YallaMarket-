package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/supermarket")
public class SupermarketController {

    private final ProductService productService;
    
    @Autowired
    public SupermarketController(ProductService productService) {
        this.productService = productService;
    }

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
     */    @GetMapping("/basket")
    public String showShoppingBasket(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "supermarket-basket";
    }
    
    /**
     * Display the product details page for a specific product
     */
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable("id") Long productId, Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        
        // Find the product in the full list of products
        List<Product> allProducts = productService.getAllProducts();
        Optional<Product> productOpt = allProducts.stream()
            .filter(p -> p.getProductId().equals(productId))
            .findFirst();
        
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "supermarket-product-details";
        } else {
            // Product not found, redirect to home
            return "redirect:/supermarket/home";
        }
    }
}
