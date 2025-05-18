package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email; // Assuming email is part of contact info validation
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserUpdateRequestDTO {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    private Integer role; // Assuming role is updated as an integer ID

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address; // Nullable

    @Size(max = 100, message = "Contact info cannot exceed 100 characters")
    private String contactInfo; // Nullable


}
