package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for returning user information in a list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {
    private Long id;
    private String username;
    private String email;
    private int role;
    private String address;
    private String contactInfo;
    private Boolean enabled;
    private Boolean isDeleted;
}
