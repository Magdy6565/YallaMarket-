package com.example.demo.dto;

/**
 * DTO for rating request
 */
public class RatingRequestDTO {
    private Integer rating;

    public RatingRequestDTO() {
    }

    public RatingRequestDTO(Integer rating) {
        this.rating = rating;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
