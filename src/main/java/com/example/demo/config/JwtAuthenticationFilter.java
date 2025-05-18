package com.example.demo.config; // Adjust package as needed

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired; // Or use constructor injection
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Assuming you use UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.JwtService;

import java.io.IOException;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

// Assuming this is your JWT Filter class
@Component // Make sure it's a Spring component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Add a logger
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Assuming you have dependencies like JwtService and UserDetailsService
    // @Autowired // Or use constructor injection
    // private JwtService jwtService; // Replace with your JWT token handling service

    // @Autowired // Or use constructor injection
    // private UserDetailsService userDetailsService; // Replace with your UserDetailsService

    // If using constructor injection, inject dependencies here:
    private final JwtService jwtService; // Example dependency
    private final UserDetailsService userDetailsService; // Example dependency

    // Example constructor injection
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // --- Add logging at the beginning of the filter ---
        logger.info("--> Entering JwtAuthenticationFilter for request: {}", request.getRequestURI());
        // -------------------------------------------------

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail; // Or username, depending on how you identify users in JWT

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If no JWT, just continue the filter chain
            logger.debug("No JWT found in Authorization header. Continuing filter chain.");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token
        jwt = authHeader.substring(7);
        logger.debug("Extracted JWT: {}", jwt);

        try {
            // Extract the user identifier (e.g., email or username) from the JWT
            // Replace with your actual method from JwtService
            userEmail = jwtService.extractUsername(jwt); // Assuming extractUsername gets the email or username

            // --- Log after extracting user identifier ---
            logger.debug("Extracted user identifier from JWT: {}", userEmail);
            // ------------------------------------------

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // If user identifier is found and no authentication is already set in context

                // Load UserDetails (your User object)
                // This is where the SELECT users query likely happens
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // --- Log after loading UserDetails ---
                logger.debug("Loaded UserDetails for user: {}", userEmail);
                // -------------------------------------

                // Validate the token (check expiration, signature, etc.)
                // Replace with your actual method from JwtService
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // If token is valid, create an Authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are null for JWT authentication after token validation
                            userDetails.getAuthorities() // Get user authorities (roles)
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set the Authentication object in the SecurityContextHolder
                    // This is crucial for the request to be considered authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // --- Log after setting authentication in SecurityContext ---
                    logger.info("Successfully authenticated user: {}", userEmail);
                    // -------------------------------------------------------

                } else {
                    // --- Log if token is invalid ---
                    logger.warn("JWT token is invalid for user: {}", userEmail);
                    // -------------------------------
                    // You might want to send a 401 Unauthorized response here
                    // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    // return;
                }
            } else {
                if (userEmail == null) {
                    logger.warn("Could not extract user identifier from JWT.");
                } else {
                    logger.debug("Authentication already exists in SecurityContext for user: {}", userEmail);
                }
            }

        } catch (Exception e) {
            // --- Log any exceptions during token processing ---
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
            // -------------------------------------------------
            // You might want to send a 401 Unauthorized response here
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // return;
        }

        // Continue the filter chain
        // If authentication was set, the request should now proceed to the controller
        // if it matches an authenticated() rule.
        logger.debug("Continuing filter chain after JWT filter.");
        filterChain.doFilter(request, response);
    }
}
