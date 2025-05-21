package com.example.demo.service.impl;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class WebAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    
    @Autowired
    public WebAuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }
    
    /**
     * Authenticate a user with username and password
     * 
     * @param username The username
     * @param password The password
     * @return True if authentication was successful, false otherwise
     */
    public boolean authenticateUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find a user by username
     * 
     * @param username The username
     * @return The user, or null if not found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
