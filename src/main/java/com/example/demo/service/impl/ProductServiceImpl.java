package com.example.demo.service.impl;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductWithVendorDTO;
import com.example.demo.dto.VendorDTO;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional // Ensure atomicity
    public Product addProduct(Long userId, ProductRequest productRequest) { // Parameter is Long userId
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setVendorId(userId); // Assign the User's ID to the vendorId field
        product.setCategory(productRequest.getCategory());
        product.setDeletedAt(null); // Ensure it's not marked as deleted on creation

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> updateProduct(Long productId, Long userId, ProductRequest productRequest) { // Parameter is Long userId
        // Call the repository method that uses the vendorId column
        Optional<Product> existingProductOptional = productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, userId);

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(productRequest.getName());
            existingProduct.setDescription(productRequest.getDescription());
            existingProduct.setPrice(productRequest.getPrice());
            existingProduct.setQuantity(productRequest.getQuantity());
            existingProduct.setCategory(productRequest.getCategory());
            // vendorId (User ID) should not be changed during update

            return Optional.of(productRepository.save(existingProduct));
        } else {
            return Optional.empty(); // Product not found or not belonging to the user/already deleted
        }
    }

    @Override
    public List<Product> getAllProductsForUser(Long userId) { // Parameter is Long userId
        // Call the repository method that uses the vendorId column
        return productRepository.findByVendorIdAndDeletedAtIsNull(userId);
    }
    
    @Override
    public List<Product> getAllProducts() {
        // Return all non-deleted products regardless of vendor
        return productRepository.findByDeletedAtIsNull();
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long productId, Long userId) { // Parameter is Long userId
        // Call the repository method that uses the vendorId column
        Optional<Product> productOptional = productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, userId);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeletedAt(LocalDateTime.now()); // Perform soft delete
            productRepository.save(product);
            return true; // Deletion successful
        } else {
            return false; // Product not found or not belonging to the user/already deleted
        }
    }

    @Override
    public List<Product> filterProductsForUser(Long userId, ProductFilterRequest filterRequest) { // Parameter is Long userId
        Integer quantity = filterRequest.getQuantity();
        String category = filterRequest.getCategory();

        if (quantity != null && category != null && !category.trim().isEmpty()) {
            // Call the repository method that uses the vendorId column
            return productRepository.findByVendorIdAndQuantityGreaterThanEqualAndCategoryAndDeletedAtIsNull(userId, quantity, category);
        } else if (quantity != null) {
            // Call the repository method that uses the vendorId column
            return productRepository.findByVendorIdAndQuantityGreaterThanEqualAndDeletedAtIsNull(userId, quantity);
        } else if (category != null && !category.trim().isEmpty()) {
            // Call the repository method that uses the vendorId column
            return productRepository.findByVendorIdAndCategoryAndDeletedAtIsNull(userId, category);
        } else {
            // If no filters are provided, return all non-deleted products for the user
            return getAllProductsForUser(userId);
        }
    }

    @Override
    public Optional<Product> getProductByIdForUser(Long productId, Long userId) { // Parameter is Long userId
        // Call the repository method that uses the vendorId column
        return productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, userId);
    }

    @Override
    public List<String> getDistinctCategories() {
        return productRepository.findDistinctCategories();
    }

    @Override
    @Transactional(readOnly = true) // Good practice for read operations
    public List<ProductWithVendorDTO> findProductsByCategoryAndVendors(
            String category, List<Long> vendorIds) {
        List<Long> effectiveVendorIds = (vendorIds != null && vendorIds.isEmpty()) ? null : vendorIds;

        List<ProductWithVendorDTO> products = productRepository.findProductsWithVendorDetails(
                category, effectiveVendorIds);

        return products;
    }

    @Override
    public List<VendorDTO> getVendorDetailsByCategory(String category) {
        return productRepository.findVendorsByCategory(category);
    }
    public Optional<Product> getProductById(Long productId) {
        // This is the implementation for the method you selected.
        // It uses the productRepository to find a product by its ID.
        return productRepository.findById(productId);
    }

}
