package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Setter
@Getter
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
    private Integer quantity;

    // This column is named 'vendor_id' in the database, but stores the User's ID
    @Column(name = "vendor_id", nullable = false) // Keep the column name as 'vendor_id'
    private Long vendorId; // Use Long type to match User.id

    @Column(name = "category")
    private String category;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Using soft delete

    // Optional: Add constructors
    public Product() {
    }

    public Product(String name, String description, BigDecimal price, Integer quantity, Long vendorId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = (quantity == null) ? 0 : quantity; // Apply default
        this.vendorId = vendorId;
        this.category = category;
        this.deletedAt = null; // Ensure not deleted by default
    }
}