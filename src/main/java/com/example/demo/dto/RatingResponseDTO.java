package com.example.demo.dto;

/**
 * DTO for rating response
 */
public class RatingResponseDTO {
    private Long id;
    private Long userId;
    private Long vendorId;
    private Integer rating;
    private String timestamp;

    public RatingResponseDTO() {
    }

    public RatingResponseDTO(Long id, Long userId, Long vendorId, Integer rating, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.vendorId = vendorId;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Static factory method for average rating response
    public static RatingResponseDTO createAverageRating(Long vendorId, Double averageRating) {
        RatingResponseDTO dto = new RatingResponseDTO();
        dto.setVendorId(vendorId);
        dto.setRating(averageRating != null ? averageRating.intValue() : 0);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
