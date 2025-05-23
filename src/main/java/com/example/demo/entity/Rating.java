//package com.example.demo.entity;
//
//import javax.persistence.*;
//import java.time.LocalDateTime;
//
///**
// * Rating entity to store vendor ratings
// */
//@Entity
//@Table(name = "vendor_ratings",
//       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vendor_id"}))
//public class Rating {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    @Column(name = "vendor_id", nullable = false)
//    private Long vendorId;
//
//    @Column(nullable = false)
//    private Integer rating;
//
//    @Column(nullable = false)
//    private LocalDateTime timestamp;
//
//    public Rating() {
//        this.timestamp = LocalDateTime.now();
//    }    /**
//     * Creates a new rating with the specified user, vendor, and rating value
//     * @param userId The ID of the user giving the rating
//     * @param vendorId The ID of the vendor being rated
//     * @param rating The rating value (1-5)
//     */
//    public Rating(Long userId, Long vendorId, Integer rating) {
//        this.userId = userId;
//        this.vendorId = vendorId;
//        this.rating = rating;
//        this.timestamp = LocalDateTime.now();
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public Long getVendorId() {
//        return vendorId;
//    }
//
//    public void setVendorId(Long vendorId) {
//        this.vendorId = vendorId;
//    }
//
//    public Integer getRating() {
//        return rating;
//    }
//
//    public void setRating(Integer rating) {
//        this.rating = rating;
//    }
//
//    public LocalDateTime getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(LocalDateTime timestamp) {
//        this.timestamp = timestamp;
//    }
//}
