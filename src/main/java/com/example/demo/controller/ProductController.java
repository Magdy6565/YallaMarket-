package com.example.demo.controller;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductWithVendorDTO;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
// Keep the base path /api/my-products (or /api/products)
@RequestMapping("/api/my-products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Helper method to get the user ID from the authenticated user
    // This method correctly gets the User's ID (Long)
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User authenticatedUser = (User) principal;
            if (authenticatedUser.getId() == null) {
                throw new IllegalStateException("Authenticated user has no ID");
            }
            return authenticatedUser.getId(); // Return the user's ID (Long)
        } else {
            throw new IllegalStateException("Authenticated principal is not a recognized User type");
        }
    }


    // Add a product
    // POST /api/my-products
    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest productRequest) {

        logger.info("We enetred add product NOWWOWOWO");
        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received POST request to add product for userId (stored as vendorId): {}", userId);
        logger.debug("Product Request Body: {}", productRequest);

        // Pass the userId (Long) to the service
        Product newProduct = productService.addProduct(userId, productRequest);

        logger.info("Product added successfully with ID: {}", newProduct.getProductId());

        return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Should return 201 Created
    }

    // Edit an existing product
    // PUT /api/my-products/{productId}
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest productRequest) {

        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received PUT request to update product {} for userId (stored as vendorId): {}", productId, userId);

        // Pass the userId (Long) to the service
        Optional<Product> updatedProduct = productService.updateProduct(productId, userId, productRequest);

        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found or not owned
    }

    // Show all products for a user
    // GET /api/my-products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsForUser() {
        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received GET request for all products for userId (stored as vendorId): {}", userId);

        // Pass the userId (Long) to the service
        List<Product> products = productService.getAllProductsForUser(userId);
        return ResponseEntity.ok(products);
    }

    // Get a single product by ID for a user
    // GET /api/my-products/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductByIdForUser(
            @PathVariable Long productId) {

        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received GET request for product {} for userId (stored as vendorId): {}", productId, userId);

        // Pass the userId (Long) to the service
        Optional<Product> product = productService.getProductByIdForUser(productId, userId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found or not owned
    }


    // Delete a certain product (soft delete)
    // DELETE /api/my-products/{productId}
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId) {

        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received DELETE request for product {} for userId (stored as vendorId): {}", productId, userId);

        // Pass the userId (Long) to the service
        boolean deleted = productService.deleteProduct(productId, userId);

        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found (or not owned/already deleted)
        }
    }

    // Filter products
    // GET /api/my-products/filter
    // Parameters can be quantity={value} and/or category={value}
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @ModelAttribute ProductFilterRequest filterRequest) {

        Long userId = getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received GET filter request for userId (stored as vendorId): {}", userId);
        logger.debug("Filter criteria: Quantity={}, Category={}", filterRequest.getQuantity(), filterRequest.getCategory());

        // Pass the userId (Long) to the service
        List<Product> filteredProducts = productService.filterProductsForUser(userId, filterRequest);
        return ResponseEntity.ok(filteredProducts);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductWithVendorDTO>> getProductsByCategoryAndVendors(
            @PathVariable @NotBlank(message = "Category cannot be null") String category,
            @RequestParam(required = false) List<Long> vendorIds) {
        List<ProductWithVendorDTO> response = productService.findProductsByCategoryAndVendors(
                category, vendorIds);
        return ResponseEntity.ok(response);
    }

}
