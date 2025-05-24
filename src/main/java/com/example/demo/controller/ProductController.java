package com.example.demo.controller;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductWithVendorDTO;
import com.example.demo.dto.VendorDTO;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import com.example.demo.util.AuthUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/my-products")
public class ProductController {    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }    /**
     * Add a product with image URL
     */
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestParam("name") String name,
                                              @RequestParam(value = "description", required = false) String description,
                                              @RequestParam("price") BigDecimal price,
                                              @RequestParam("quantity") Integer quantity,
                                              @RequestParam(value = "category", required = false) String category,
                                              @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        
        try {
            logger.info("Adding product with image URL: {}", imageUrl);
            
            // Build request
            ProductRequest req = new ProductRequest();
            req.setName(name);
            req.setDescription(description);
            req.setPrice(price);
            req.setQuantity(quantity);
            req.setCategory(category);
            
            // Save product with image URL
            Product saved = productService.addProductWithImageUrl(userId, req, imageUrl);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to add product with image URL: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Edit an existing product
    // PUT /api/my-products/{productId}
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest productRequest) {

        Long userId = AuthUtil.getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received PUT request to update product {} for userId (stored as vendorId): {}", productId, userId);

        // Pass the userId (Long) to the service
        Optional<Product> updatedProduct = productService.updateProduct(productId, userId, productRequest);

        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found or not owned
    }

    /**
     * Update a product with image URL
     */
    @PutMapping("/{productId}/url")
    public ResponseEntity<Product> updateProductWithUrl(
            @PathVariable Long productId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {

        Long userId = AuthUtil.getAuthenticatedUserId();
        logger.info("Received PUT request to update product {} with image URL for userId: {}", productId, userId);

        try {
            // Build request
            ProductRequest req = new ProductRequest();
            req.setName(name);
            req.setDescription(description);
            req.setPrice(price);
            req.setQuantity(quantity);
            req.setCategory(category);

            // Update product with image URL
            Optional<Product> updatedProduct = productService.updateProductWithImageUrl(productId, userId, req, imageUrl);

            return updatedProduct.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Failed to update product with image URL: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Show all products for a user
    // GET /api/my-products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsForUser() {
        Long userId = AuthUtil.getAuthenticatedUserId(); // Get userId (Long) from authenticated user
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

        Long userId = AuthUtil.getAuthenticatedUserId(); // Get userId (Long) from authenticated user
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

        Long userId = AuthUtil.getAuthenticatedUserId(); // Get userId (Long) from authenticated user
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

        Long userId = AuthUtil.getAuthenticatedUserId(); // Get userId (Long) from authenticated user
        logger.info("Received GET filter request for userId (stored as vendorId): {}", userId);
        logger.debug("Filter criteria: Quantity={}, Category={}", filterRequest.getQuantity(), filterRequest.getCategory());

        // Pass the userId (Long) to the service
        List<Product> filteredProducts = productService.filterProductsForUser(userId, filterRequest);
        return ResponseEntity.ok(filteredProducts);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategories() {
        List<String> categories = productService.getDistinctCategories();
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

    @GetMapping("/vendors-by-category/{category}")
    public ResponseEntity<List<VendorDTO>> getVendorsByCategory(@PathVariable String category) {
        List<VendorDTO> vendors = productService.getVendorDetailsByCategory(category);
        return ResponseEntity.ok(vendors);
    }

}
