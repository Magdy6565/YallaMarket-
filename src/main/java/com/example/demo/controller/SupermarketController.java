package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
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
public class SupermarketController {    private final ProductService productService;
    private final UserService userService;
    
    public SupermarketController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
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
    }    @GetMapping("/product/{id}")
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
                Product product = productOpt.get();

                // Fetch vendor name if vendorId is available
                if (product.getVendorId() != null) {
                    Optional<User> vendorOpt = userService.findById(product.getVendorId());
                    if (vendorOpt.isPresent()) {
                        String vendorName = vendorOpt.get().getUsername();
                        model.addAttribute("vendorName", vendorName);
                    }
                    model.addAttribute("vendorId", product.getVendorId());
                }

                model.addAttribute("product", product);
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
//@GetMapping("/product/{id}")
//public String getProductDetails(@PathVariable Long id, Model model) {
//    Optional<Product> product = productService.getProductById(id); // Or some other way of fetching the product
//    model.addAttribute("product", product);
//    return "supermarket-product-details";
//}
}
