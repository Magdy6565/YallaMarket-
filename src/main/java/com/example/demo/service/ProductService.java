package com.example.demo.service;

import com.example.demo.dto.ProductFilterRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product addProduct(Integer vendorId, ProductRequest productRequest);

    Optional<Product> updateProduct(Long productId, Integer vendorId, ProductRequest productRequest);

    List<Product> getAllProductsForVendor(Integer vendorId);

    boolean deleteProduct(Long productId, Integer vendorId);

    List<Product> filterProductsForVendor(Integer vendorId, ProductFilterRequest filterRequest);

    Optional<Product> getProductByIdForVendor(Long productId, Integer vendorId);
}