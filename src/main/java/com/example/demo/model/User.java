package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal; // Import BigDecimal for rating
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

//map it to the users table
@Entity
@Table(name = "users")
//------------------------
//These will add setters and getters thanks to lombock
@Getter
@Setter
@NoArgsConstructor
//UserDetails --> integrates spring security
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Changed from AUTO, assuming SERIAL in DB
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email; // Assuming this is the contact email

    @Column(nullable = false)
    private String password; // This should store password_hash

    @Column(name = "jwt_token") // Assuming this column exists based on schema comment
    private String jwtToken;

    @Column(name = "role")
    private int role; // Assuming role is stored as an integer

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;

    private boolean enabled;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "deleted_at") // Assuming this exists based on schema comment
    private LocalDateTime deletedAt;

    // You can add other constructors as needed, e.g., for registration
    public User(String username, String email, String password, int role, String address, String contactInfo) {
        this.username = username;
        this.email = email; // Using email for the email column
        this.password = password; // Assuming this is the hashed password
        this.role = role;
        this.address = address;
        this.contactInfo = contactInfo;
        this.enabled = true; // Assuming enabled by default or handle separately
        this.rating = BigDecimal.ZERO; // Assuming default rating is 0.0 or handle separately
        // vendorId, storeId, deletedAt, verification fields would be set separately or be nullable
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // You would typically return authorities based on the 'role' field here
        // For example, using a simple mapping or enum
        return List.of(); // Placeholder - Implement actual role-based authorities
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // UserDetails requires getUsername, but your DB has both username and email.
        // You might need to decide which one is used for Spring Security login,
        // or provide a custom UserDetailsService.
        // Returning the 'username' field from your entity here.
        return this.username;
//        return this.email;
    }



    // TODO: add proper boolean checks based on deletedAt and enabled
    @Override
    public boolean isAccountNonExpired() {
        return deletedAt == null; // Account is non-expired if not soft deleted
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implement locking logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implement credential expiration logic if needed
    }

    @Override
    public boolean isEnabled() {
        return enabled && (deletedAt == null); // Account is enabled if 'enabled' is true AND not soft deleted
    }

    // Lombok's @Getter and @Setter handle the rest of the methods.
    // If you remove Lombok, you'll need to manually add getters/setters for all fields.
}