package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
//UserDetails --> integrates spring security
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "verification_code")
    private String verificationCode;
    @Column(name = "role")
    private int role;
    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;
    private boolean enabled;
    //------------------------------

    //constructor for creating an unverified user
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    //default constructor
    public User(){
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    //TODO: add proper boolean checks
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Check if the user has a specific role
     * 
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(UserRole role) {
        return this.role == role.getValue();
    }
    
    /**
     * Get the user's role as an enum
     * 
     * @return The user's role as a UserRole enum
     */
    public UserRole getUserRole() {
        return UserRole.fromValue(this.role);
    }
}