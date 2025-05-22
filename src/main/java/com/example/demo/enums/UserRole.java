package com.example.demo.enums;

public enum UserRole {
    ADMIN(0),
    VENDOR(1),
    RETAIL_STORE(2),
    CUSTOMER(3);
    
    private final int value;
    
    UserRole(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static UserRole fromValue(int value) {
        for (UserRole role : UserRole.values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid UserRole value: " + value);
    }
}
