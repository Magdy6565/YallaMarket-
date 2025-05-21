package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
@Setter
@Getter
public class RegisterUserDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address; // New field

    @Size(max = 100, message = "Contact info cannot exceed 100 characters")
    private String contactInfo; // New field

    private Integer role;

}