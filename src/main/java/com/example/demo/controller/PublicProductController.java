package com.example.demo.controller;

import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for public product APIs
 * These endpoints are accessible without authentication
 */
@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    private static final Logger logger = LoggerFactory.getLogger(PublicProductController.class);
    
    @PostConstruct
    public void init() {
        logger.info("PublicProductController initialized with path /api/products");
    }    private final ProductService productService;

    @Autowired
    public PublicProductController(ProductService productService) {
        this.productService = productService;
        logger.info("PublicProductController constructor called with productService: {}", productService);
    }
    
    /**
     * Simple test endpoint
     * @return A test message
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("PublicProductController is working!");
    }

    /**
     * Get all products (public endpoint)
     * @return List of all non-deleted products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Received GET request for all products (public endpoint)");
        
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get a single product by ID (public endpoint)
     * @param productId The ID of the product to retrieve
     * @return The product if found, or 404
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        logger.info("Received GET request for product {} (public endpoint)", productId);
        
        // Since we don't have a getProductById method without user ID, we'll use a workaround
        // This isn't ideal but will work for testing purposes
        Optional<Product> product = Optional.empty();
        
        // Find the product in the full list of products
        List<Product> allProducts = productService.getAllProducts();
        for (Product p : allProducts) {
            if (p.getProductId().equals(productId)) {
                product = Optional.of(p);
                break;
            }
        }
        
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new product (public endpoint)
     * @param productRequest The product data
     * @return The created product
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        logger.info("Received POST request to create product (public endpoint)");
        
        // For testing purposes, we'll use a default vendor ID (1)
        // In production, this should be handled with proper authentication
        Long defaultVendorId = 1L;
        
        Product newProduct = productService.addProduct(defaultVendorId, productRequest);
        logger.info("Product created successfully with ID: {}", newProduct.getProductId());
        
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }
}
