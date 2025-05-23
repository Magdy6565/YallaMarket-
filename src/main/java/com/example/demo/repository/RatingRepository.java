package com.example.demo.repository;

import com.example.demo.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find rating by user ID and vendor ID
     * 
     * @param userId User who rated
     * @param vendorId Vendor who was rated
     * @return The rating or empty if not found
     */
    Optional<Rating> findByUserIdAndVendorId(Long userId, Long vendorId);
    
    /**
     * Calculate the average rating for a vendor
     * 
     * @param vendorId Vendor ID
     * @return Average rating or null if no ratings
     */
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.vendorId = :vendorId")
    Double getAverageRatingForVendor(@Param("vendorId") Long vendorId);
}
