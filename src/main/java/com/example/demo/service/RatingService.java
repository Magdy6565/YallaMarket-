package com.example.demo.service;

import com.example.demo.dto.RatingRequestDTO;
import com.example.demo.dto.RatingResponseDTO;
import com.example.demo.model.Rating;
import com.example.demo.model.User;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service for managing vendor ratings
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Rate a vendor or update an existing rating
     * 
     * @param vendorId The ID of the vendor being rated
     * @param ratingRequest The rating request with rating value
     * @return The created or updated rating
     */
    public RatingResponseDTO rateVendor(Long vendorId, RatingRequestDTO ratingRequest) {
        // Validate the rating value (1-5)
        if (ratingRequest.getRating() == null || ratingRequest.getRating() < 1 || ratingRequest.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to rate vendors");
        }
        
        // Check if the user and vendor exist
        User currentUser = getUserFromAuth(auth);
        
        // Prevent vendors from rating themselves
        if (currentUser.getId().equals(vendorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot rate yourself");
        }
        
        // Check if the user has already rated this vendor (update existing rating)
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndVendorId(currentUser.getId(), vendorId);
        
        Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setRating(ratingRequest.getRating());
        } else {
            rating = new Rating(currentUser.getId(), vendorId, ratingRequest.getRating());
        }
        
        rating = ratingRepository.save(rating);
        return convertToDTO(rating);
    }

    /**
     * Get the current user's rating for a specific vendor
     * 
     * @param vendorId The vendor ID
     * @return The user's rating or empty response if no rating
     */
    public RatingResponseDTO getUserVendorRating(Long vendorId) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to view your ratings");
        }
        
        User currentUser = getUserFromAuth(auth);
        
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndVendorId(currentUser.getId(), vendorId);
        
        return existingRating.map(this::convertToDTO)
                             .orElse(new RatingResponseDTO(null, currentUser.getId(), vendorId, null, null));
    }    /**
     * Get the average rating for a vendor
     * 
     * @param vendorId The vendor ID
     * @return The average rating
     */
    public RatingResponseDTO getVendorAverageRating(Long vendorId) {
        Double averageRating = ratingRepository.getAverageRatingForVendor(vendorId);
        
        // If no ratings yet, return 0 instead of null
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // Round to 1 decimal place for display purposes
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;
        
        return RatingResponseDTO.createAverageRating(vendorId, roundedRating);
    }
    
    /**
     * Helper method to convert Rating entity to DTO
     */
    private RatingResponseDTO convertToDTO(Rating rating) {
        return new RatingResponseDTO(
                rating.getId(),
                rating.getUserId(),
                rating.getVendorId(),
                rating.getRating(),
                rating.getTimestamp().format(DATE_FORMATTER)
        );
    }
    
    /**
     * Helper method to get user from authentication
     */
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
