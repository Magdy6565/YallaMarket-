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
        
        try {
            // Directly fetch the product by ID using the public endpoint
            Optional<Product> productOpt = Optional.empty();

            // First try to find the product in the full list of products
            List<Product> allProducts = productService.getAllProducts();
            productOpt = allProducts.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst();
            
            if (productOpt.isPresent()) {
                // Add vendor name to the product if available
                Product product = productOpt.get();
                
                // Ensure the vendorId is correctly set in the model
                if (product.getVendorId() != null) {
                    // You might want to add more vendor information here if needed
                    // For now, we're just ensuring the vendorId is available
                    model.addAttribute("vendorId", product.getVendorId());
                }
                
                model.addAttribute("product", product);
//                return "Hello ziad shawky" ;
                return "supermarket-product-details";
            } else {
                // Product not found, redirect to home
                return "redirect:/supermarket/home";
            }
        } catch (Exception e) {
            // Log error and redirect to home
            System.err.println("Error fetching product details: " + e.getMessage());
            return "redirect:/supermarket/home";
        }
    }
}
