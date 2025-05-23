package com.example.demo.controller;

import com.example.demo.dto.RatingRequestDTO;
import com.example.demo.dto.RatingResponseDTO;
import com.example.demo.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API Controller for managing vendor ratings
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Submit a new rating for a vendor
     * 
     * @param vendorId The ID of the vendor being rated
     * @param ratingRequest The rating request with rating value
     * @return The newly created rating
     */
    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<RatingResponseDTO> rateVendor(
            @PathVariable Long vendorId,
            @RequestBody RatingRequestDTO ratingRequest) {
        
        try {
            logger.info("Rating vendor: {} with rating: {}", vendorId, ratingRequest.getRating());
            RatingResponseDTO rating = ratingService.rateVendor(vendorId, ratingRequest);
            return ResponseEntity.ok(rating);
        } catch (ResponseStatusException e) {
            logger.error("Error rating vendor: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error rating vendor", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing rating: " + e.getMessage());
        }
    }

    /**
     * Get the current user's rating for a specific vendor
     * 
     * @param vendorId The ID of the vendor
     * @return The user's rating or empty if no rating exists
     */
    @GetMapping("/vendor/{vendorId}/user")
    public ResponseEntity<RatingResponseDTO> getUserVendorRating(@PathVariable Long vendorId) {
        try {
            logger.info("Getting user rating for vendor: {}", vendorId);
            RatingResponseDTO rating = ratingService.getUserVendorRating(vendorId);
            return ResponseEntity.ok(rating);
        } catch (ResponseStatusException e) {
            // If the user is not authenticated, just return an empty rating
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.info("User not authenticated, returning empty rating");
                return ResponseEntity.ok(new RatingResponseDTO(null, null, vendorId, null, null));
            }
            logger.error("Error getting user rating: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error getting user rating", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting rating: " + e.getMessage());
        }
    }

    /**
     * Get the average rating for a vendor
     * 
     * @param vendorId The ID of the vendor
     * @return The vendor's average rating
     */
    @GetMapping("/vendor/{vendorId}/average")
    public ResponseEntity<RatingResponseDTO> getVendorAverageRating(@PathVariable Long vendorId) {
        try {
            logger.info("Getting average rating for vendor: {}", vendorId);
            RatingResponseDTO averageRating = ratingService.getVendorAverageRating(vendorId);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            logger.error("Error getting average rating", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting average rating: " + e.getMessage());
        }
    }
}
