package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Product entities.
 * Leverages Spring Data JPA for database operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVendorIdAndDeletedAtIsNull(Integer vendorId);
    Optional<Product> findByProductIdAndVendorIdAndDeletedAtIsNull(Long productId, Integer vendorId);
    List<Product> findByVendorIdAndQuantityGreaterThanEqualAndDeletedAtIsNull(Integer vendorId, Integer quantity);
    List<Product> findByVendorIdAndCategoryAndDeletedAtIsNull(Integer vendorId, String category);
    List<Product> findByVendorIdAndQuantityGreaterThanEqualAndCategoryAndDeletedAtIsNull(Integer vendorId, Integer quantity, String category);

}