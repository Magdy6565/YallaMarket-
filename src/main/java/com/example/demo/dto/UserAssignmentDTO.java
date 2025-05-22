package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a user to a vendor/store
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAssignmentDTO {
    private Long userId;
    private Long vendorId;  // ID of the vendor/store to assign the user to
}
