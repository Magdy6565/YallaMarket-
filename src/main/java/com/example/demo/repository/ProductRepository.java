package com.example.demo.repository;

import com.example.demo.dto.ProductWithVendorDTO;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 3ayzha dee 3l4an el vendor statisticss
    Long countByVendorId(Long vendorId);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.deletedAt IS NULL")
    List<String> findDistinctCategories();

    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.deletedAt IS NULL AND p.quantity >= :quantityRequested")
    Optional<Product> findByProductIdAndAvailableQuantity(@Param("productId") Long productId, @Param("quantityRequested") Integer quantityRequested);

    @Query("""
                SELECT new com.example.demo.dto.ProductWithVendorDTO(
                    p.productId, p.name, p.description, p.price, p.quantity, p.category,
                    u.id, u.username, u.email, u.address, u.rating
                )
                FROM Product p
                JOIN User u ON p.vendorId = u.id
                WHERE p.category = :category
                AND p.deletedAt IS NULL
                AND u.deletedAt IS NULL
                AND (:vendorIds IS NULL OR p.vendorId IN :vendorIds)
            """)
    List<ProductWithVendorDTO> findProductsWithVendorDetails(
            @Param("category") String category,
            @Param("vendorIds") List<Long> vendorIds

    );
}
