package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Product entities.
 * Leverages Spring Data JPA for database operations.
 * Products are associated with a User via the 'vendor_id' column which stores the User's ID.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByVendorIdAndDeletedAtIsNull(Long userId); // Use findByVendorId, parameter is Long userId

    Optional<Product> findByProductIdAndVendorIdAndDeletedAtIsNull(Long productId, Long userId); // Use findByVendorId, parameter is Long userId

    List<Product> findByVendorIdAndQuantityGreaterThanEqualAndDeletedAtIsNull(Long userId, Integer quantity); // Use findByVendorId, parameter is Long userId

    List<Product> findByVendorIdAndCategoryAndDeletedAtIsNull(Long userId, String category); // Use findByVendorId, parameter is Long userId

    List<Product> findByVendorIdAndQuantityGreaterThanEqualAndCategoryAndDeletedAtIsNull(Long userId, Integer quantity, String category); // Use findByVendorId, parameter is Long userId

    // Note: Soft delete is handled in the Service layer by updating the 'deletedAt' field.
    // The 'deletedAtIsNull' condition is added to find methods to exclude soft-deleted items.
}
