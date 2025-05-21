package com.example.demo.dto;

import com.example.demo.model.Invoice;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class VendorOrderDetailsDto {

    private Long orderId;
    private Long storeId;
    private LocalDate orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<VendorOrderItemDto> orderItems; // List of simplified order items
    private InvoiceDTO invoice;

    // Constructor to map from Order entity
    public VendorOrderDetailsDto(Order order) {
        this.orderId = order.getOrderId();
        this.storeId = order.getUserId();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        // Map OrderItems to VendorOrderItemDto
        if (order.getOrderItems() != null) {
            this.orderItems = order.getOrderItems().stream()
                    .filter(item -> item.getDeletedAt() == null) // Exclude soft-deleted items
                    .map(VendorOrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    public VendorOrderDetailsDto(Order order, Invoice invoice) {
        this(order); // Call the main constructor first
        if (invoice != null && invoice.getDeletedAt() == null) {
            this.invoice = new InvoiceDTO(invoice);
        }
    }


    public static class VendorOrderItemDto {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private String productCategory;
        private Integer quantity;
        private BigDecimal priceEach;

        // Constructor to map from OrderItem entity
        public VendorOrderItemDto(OrderItem orderItem) {
            this.orderItemId = orderItem.getOrderItemId();
            this.productId = orderItem.getProductId();
            this.quantity = orderItem.getQuantity();
            this.priceEach = orderItem.getPriceEach();
            // Include product details if the product relationship is loaded
            if (orderItem.getProduct() != null) {
                Product product = orderItem.getProduct();
                this.productName = product.getName();
                this.productCategory = product.getCategory();
                // Note: product.vendorId is not included here as it's implicit
                // that these items belong to the vendor's products.
            }
        }

        // Getters (Setters are not needed for a response DTO)
        public Long getOrderItemId() { return orderItemId; }
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductCategory() { return productCategory; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getPriceEach() { return priceEach; }
    }
}