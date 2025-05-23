package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orderitems")
@Setter
@Getter
@NoArgsConstructor
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "orderItemId"
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming SERIAL in DB
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "order_id", nullable = false)
    private Long orderId; // Foreign key to Orders

    @Column(name = "product_id", nullable = false)
    private Long productId; // Foreign key to Products

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_each", nullable = false)
    private BigDecimal priceEach;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // Relationships
    // Many OrderItems belong to one Order
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is generally good for performance
    @JoinColumn(name = "order_id", insertable = false, updatable = false) // order_id is the foreign key column
    @JsonIgnoreProperties("orderItems")
    private Order order; // Reference to the parent Order

    // Many OrderItems are for one Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false) // product_id is the foreign key column
    @JsonIgnoreProperties("orderItems")
    private Product product; // Reference to the associated Product
      @OneToMany(mappedBy = "orderItem")
    @JsonIgnoreProperties("orderItem") 
    private List<Refund> refunds = new ArrayList<>();

}