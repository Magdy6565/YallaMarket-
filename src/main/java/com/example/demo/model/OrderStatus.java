// src/main/java/com/example/demo/model/OrderStatus.java
package com.example.demo.model;

public enum OrderStatus {
    PENDING(1),
    APPROVED(2),
    SHIPPED(3),
    DELIVERED(4),
    CANCELLED(5),
    DENIED(6);

    private final int statusCode;

    OrderStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static OrderStatus fromStatusCode(int code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.statusCode == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
    }
}