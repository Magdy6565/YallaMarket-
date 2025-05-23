package com.example.demo.service;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductWithVendorDTO;
import com.example.demo.dto.VendorDTO;
import com.example.demo.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Interface for the Product Service, defining the business operations related to Products.
 * Products are associated with a User via the 'vendor_id' column which stores the User's ID.
 */
public interface ProductService {

    Product addProduct(Long userId, ProductRequest productRequest); // Parameter is Long userId

    Optional<Product> updateProduct(Long productId, Long userId, ProductRequest productRequest); // Parameter is Long userId

    List<Product> getAllProductsForUser(Long userId); // Parameter is Long userId
    
    List<Product> getAllProducts(); // Method to get all products regardless of vendor

    boolean deleteProduct(Long productId, Long userId); // Parameter is Long userId

    List<Product> filterProductsForUser(Long userId, ProductFilterRequest filterRequest); // Parameter is Long userId

    Optional<Product> getProductByIdForUser(Long productId, Long userId); // Parameter is Long userId

    List<String> getDistinctCategories();

    List<ProductWithVendorDTO> findProductsByCategoryAndVendors(String category, List<Long> vendorIds);

    List<VendorDTO> getVendorDetailsByCategory(String category);

    Optional<Product> getProductById(Long productId);

}
