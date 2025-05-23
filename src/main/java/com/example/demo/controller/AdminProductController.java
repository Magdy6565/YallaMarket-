package com.example.demo.controller;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for admin product management operations
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    
    private final ProductService productService;
    
    @Autowired
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Get all products with pagination and filtering
     * 
     * @param page Page number (0-based)
     * @param size Items per page
     * @param category Filter by category
     * @param vendorId Filter by vendor ID
     * @param productId Filter by product ID
     * @return Page of products
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long productId) {
        
        logger.info("Admin fetching all products with filters: page={}, size={}, category={}, vendorId={}, productId={}",
                page, size, category, vendorId, productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("productId").descending());
        
        ProductFilterRequest filterRequest = new ProductFilterRequest();
        if (category != null && !category.isEmpty()) {
            filterRequest.setCategory(category);
        }
        
        Page<Product> products;
          if (productId != null) {
            // If product ID is provided, fetch just that product (including soft-deleted for admin)
            Optional<Product> product = productService.getProductByIdAsAdmin(productId);
            if (product.isPresent()) {
                products = new org.springframework.data.domain.PageImpl<>(
                        List.of(product.get()), pageable, 1);
            } else {
                products = Page.empty(pageable);
            }
        } else {
            // Otherwise, apply filters
            products = productService.getAllProductsWithFilters(filterRequest, vendorId, pageable);
        }
        
        return ResponseEntity.ok(products);
    }
    
    /**
     * Get a specific product by ID
     * 
     * @param productId Product ID
     * @return The product
     */    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        logger.info("Admin fetching product with ID: {}", productId);
        
        Optional<Product> product = productService.getProductByIdAsAdmin(productId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Add a new product
     * 
     * @param productRequest Product data
     * @return The created product
     */    @PostMapping
    public ResponseEntity<Product> addProduct(
            @Valid @RequestBody ProductRequest productRequest,
            @RequestParam Long vendorId) {
        logger.info("Admin adding new product: {} for vendor: {}", productRequest.getName(), vendorId);
        
        Product newProduct = productService.addProduct(vendorId, productRequest);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing product
     * 
     * @param productId Product ID
     * @param productRequest Updated product data
     * @return The updated product
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest productRequest) {
        
        logger.info("Admin updating product with ID: {}", productId);
        
        Optional<Product> updatedProduct = productService.updateProductAsAdmin(productId, productRequest);
        return updatedProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Soft-delete a product
     * 
     * @param productId Product ID
     * @return No content on success
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        logger.info("Admin deleting product with ID: {}", productId);
        
        boolean deleted = productService.deleteProductAsAdmin(productId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    
    /**
     * Restore a soft-deleted product
     * 
     * @param productId Product ID
     * @return The restored product
     */
    @PutMapping("/{productId}/restore")
    public ResponseEntity<Product> restoreProduct(@PathVariable Long productId) {
        logger.info("Admin restoring product with ID: {}", productId);
        
        Optional<Product> restoredProduct = productService.restoreProductAsAdmin(productId);
        return restoredProduct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Get all distinct categories
     * 
     * @return List of categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        logger.info("Admin fetching all categories");
        
        List<String> categories = productService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }
}
