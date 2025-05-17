package com.example.demo.service.impl;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
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
    public Product addProduct(Integer vendorId, ProductRequest productRequest) {
        Product product = new Product();
        System.out.println("Hello inside add product");
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setVendorId(vendorId); // Assign the vendor ID
        product.setCategory(productRequest.getCategory());
        product.setDeletedAt(null); // Ensure it's not marked as deleted on creation

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> updateProduct(Long productId, Integer vendorId, ProductRequest productRequest) {
        Optional<Product> existingProductOptional = productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, vendorId);

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(productRequest.getName());
            existingProduct.setDescription(productRequest.getDescription());
            existingProduct.setPrice(productRequest.getPrice());
            existingProduct.setQuantity(productRequest.getQuantity());
            existingProduct.setCategory(productRequest.getCategory());

            return Optional.of(productRepository.save(existingProduct));
        } else {
            return Optional.empty(); // Product not found or not belonging to the vendor/already deleted
        }
    }

    @Override
    public List<Product> getAllProductsForVendor(Integer vendorId) {
        return productRepository.findByVendorIdAndDeletedAtIsNull(vendorId);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long productId, Integer vendorId) {
        Optional<Product> productOptional = productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, vendorId);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeletedAt(LocalDateTime.now()); // Perform soft delete
            productRepository.save(product);
            return true; // Deletion successful
        } else {
            return false; // Product not found or not belonging to the vendor/already deleted
        }
    }

    @Override
    public List<Product> filterProductsForVendor(Integer vendorId, ProductFilterRequest filterRequest) {
        Integer quantity = filterRequest.getQuantity();
        String category = filterRequest.getCategory();

        if (quantity != null && category != null && !category.trim().isEmpty()) {
            return productRepository.findByVendorIdAndQuantityGreaterThanEqualAndCategoryAndDeletedAtIsNull(vendorId, quantity, category);
        } else if (quantity != null) {
            return productRepository.findByVendorIdAndQuantityGreaterThanEqualAndDeletedAtIsNull(vendorId, quantity);
        } else if (category != null && !category.trim().isEmpty()) {
            return productRepository.findByVendorIdAndCategoryAndDeletedAtIsNull(vendorId, category);
        } else {
            // If no filters are provided, return all non-deleted products for the vendor
            return getAllProductsForVendor(vendorId);
        }
    }

    @Override
    public Optional<Product> getProductByIdForVendor(Long productId, Integer vendorId) {
        return productRepository.findByProductIdAndVendorIdAndDeletedAtIsNull(productId, vendorId);
    }
}