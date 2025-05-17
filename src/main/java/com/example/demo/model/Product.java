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

    @Column(name = "vendor_id", nullable = false)
    private Integer vendorId; // Assuming vendor_id in Product maps to an Integer

    @Column(name = "category")
    private String category;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Using soft delete

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }



    public String getName() {
        return name;
    }



    public String getDescription() {
        return description;
    }



    public BigDecimal getPrice() {
        return price;
    }



    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.quantity == null) {
            this.quantity = 0; // Ensure default 0 if not provided
        }
    }

    public Integer getVendorId() {
        return vendorId;
    }


    public String getCategory() {
        return category;
    }


    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }



    // Optional: Add constructors
    public Product() {
    }

    public Product(String name, String description, BigDecimal price, Integer quantity, Integer vendorId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = (quantity == null) ? 0 : quantity; // Apply default
        this.vendorId = vendorId;
        this.category = category;
        this.deletedAt = null; // Ensure not deleted by default
    }
}