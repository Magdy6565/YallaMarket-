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
    }    @Override
    public List<VendorDTO> getVendorDetailsByCategory(String category) {
        return productRepository.findVendorsByCategory(category);
    }
      @Override
    public org.springframework.data.domain.Page<Product> getAllProductsWithFilters(ProductFilterRequest filterRequest, Long vendorId, org.springframework.data.domain.Pageable pageable) {
        // Include all products (including soft-deleted ones) for admin to manage
        if (filterRequest.getCategory() != null && !filterRequest.getCategory().isEmpty() && vendorId != null) {
            // Filter by both category and vendor ID
            return productRepository.findByCategoryAndVendorId(
                    filterRequest.getCategory(), vendorId, pageable);
        } else if (filterRequest.getCategory() != null && !filterRequest.getCategory().isEmpty()) {
            // Filter by category only
            return productRepository.findByCategory(filterRequest.getCategory(), pageable);
        } else if (vendorId != null) {
            // Filter by vendor ID only
            return productRepository.findByVendorId(vendorId, pageable);
        } else {
            // No filters, get all products including soft-deleted ones
            return productRepository.findAll(pageable);
        }
    }

    @Override
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getDeletedAt() == null);
    }

    @Override
    public Optional<Product> getProductByIdAsAdmin(Long productId) {
        // Admin can view any product, including soft-deleted ones
        return productRepository.findById(productId);
    }

    @Override
    @Transactional
    public Optional<Product> updateProductAsAdmin(Long productId, ProductRequest productRequest) {
        Optional<Product> existingProductOptional = productRepository.findById(productId)
                .filter(product -> product.getDeletedAt() == null);        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(productRequest.getName());
            existingProduct.setDescription(productRequest.getDescription());
            existingProduct.setPrice(productRequest.getPrice());
            existingProduct.setQuantity(productRequest.getQuantity());
            existingProduct.setCategory(productRequest.getCategory());
            // Product doesn't have an updatedAt field
            
            return Optional.of(productRepository.save(existingProduct));
        }
        
        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean deleteProductAsAdmin(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId)
                .filter(product -> product.getDeletedAt() == null);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeletedAt(LocalDateTime.now());
            productRepository.save(product);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public Optional<Product> restoreProductAsAdmin(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId)
                .filter(product -> product.getDeletedAt() != null);
          if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeletedAt(null); // Clear the deletedAt field to restore
            // Product doesn't have an updatedAt field
            return Optional.of(productRepository.save(product));
        }
        
        return Optional.empty();
    }    @Override
    @Transactional
    public Product addProductWithImageUrl(Long userId, ProductRequest productRequest, String imageUrl) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setVendorId(userId);
        product.setCategory(productRequest.getCategory());
        product.setImageUrl(imageUrl);
        product.setDeletedAt(null);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> updateProductWithImageUrl(Long productId, Long userId, ProductRequest productRequest, String imageUrl) {
        Optional<Product> existingProductOptional = productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, userId);

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(productRequest.getName());
            existingProduct.setDescription(productRequest.getDescription());
            existingProduct.setPrice(productRequest.getPrice());
            existingProduct.setQuantity(productRequest.getQuantity());
            existingProduct.setCategory(productRequest.getCategory());
            existingProduct.setImageUrl(imageUrl);
            
            return Optional.of(productRepository.save(existingProduct));
        } else {
            return Optional.empty();
        }
    }
}
