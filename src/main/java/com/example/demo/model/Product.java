package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity = 0;

    // This column is named 'vendor_id' in the database, but stores the User's ID
    @Column(name = "vendor_id", nullable = false) // Keep the column name as 'vendor_id'
    private Long vendorId; // Use Long type to match User.id    @Column(name = "category")
    private String category;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Using soft delete
    
    @Column(name = "image_url")
    private String imageUrl;
    
    /**
     * JSON-friendly getter for the image URL
     */
    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }
}