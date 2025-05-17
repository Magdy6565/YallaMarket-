package com.example.demo.controller;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // For using validation annotations

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger; // Import the Logger class
import org.slf4j.LoggerFactory; // Import the LoggerFactory class

@RestController
@RequestMapping("/api/vendors/{vendorId}/products") // Base path including vendor ID
public class ProductController {

    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Add a product
    // POST /api/vendors/{vendorId}/products
    @PostMapping
    public ResponseEntity<Product> addProduct(
            @PathVariable Integer vendorId,
            @Valid @RequestBody ProductRequest productRequest) {

        // --- Add this log line here ---
        logger.info("Received POST request to add product for vendorId: {}", vendorId);
        logger.debug("Product Request Body: {}", productRequest); // Optional: log the body

        Product newProduct = productService.addProduct(vendorId, productRequest);

        // --- Add this log line before returning ---
        logger.info("Product added successfully with ID: {}", newProduct.getProductId());


        return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Should return 201 Created
    }

    // Edit an existing product
    // PUT /api/vendors/{vendorId}/products/{productId}
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Integer vendorId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest productRequest) {

        Optional<Product> updatedProduct = productService.updateProduct(productId, vendorId, productRequest);

        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Show all products for a vendor
    // GET /api/vendors/{vendorId}/products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsForVendor(@PathVariable Integer vendorId) {
        List<Product> products = productService.getAllProductsForVendor(vendorId);
        return ResponseEntity.ok(products);
    }

    // Get a single product by ID for a vendor
    // GET /api/vendors/{vendorId}/products/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductByIdForVendor(
            @PathVariable Integer vendorId,
            @PathVariable Long productId) {
        Optional<Product> product = productService.getProductByIdForVendor(productId, vendorId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Delete a certain product (soft delete)
    // DELETE /api/vendors/{vendorId}/products/{productId}
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer vendorId,
            @PathVariable Long productId) {
        boolean deleted = productService.deleteProduct(productId, vendorId);

        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found (or product not owned by vendor)
        }
    }

    // Filter products
    // GET /api/vendors/{vendorId}/products/filter
    // Parameters can be quantity={value} and/or category={value}
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @PathVariable Integer vendorId,
            @ModelAttribute ProductFilterRequest filterRequest) { // Use @ModelAttribute for query params
        List<Product> filteredProducts = productService.filterProductsForVendor(vendorId, filterRequest);
        return ResponseEntity.ok(filteredProducts);
    }
}