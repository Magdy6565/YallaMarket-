package com.example.demo.service;

import com.example.demo.model.User; // Import your User model
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory


@Service
public class JwtService {

    // Add a logger
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);


    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Extracts the subject claim from the JWT.
     * In this application's design, the subject claim stores the user's EMAIL address.
     * Although the method is named 'extractUsername', it extracts the subject claim,
     * which is configured to be the user's email in this application.
     * @param token The JWT token.
     * @return The subject claim, which is the user's email address.
     */
    public String extractUsername(String token) { // Keeping original method name as requested
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT.
     * @param token The JWT token.
     * @param claimsResolver A function to resolve the desired claim from the Claims object.
     * @param <T> The type of the claim to extract.
     * @return The value of the extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the given UserDetails.
     * @param userDetails The UserDetails object (your User entity) containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with extra claims for the given UserDetails.
     * @param extraClaims Additional claims to include in the token payload.
     * @param userDetails The UserDetails object (your User entity) containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Gets the configured JWT expiration time in milliseconds.
     * @return The expiration time.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Builds the JWT token.
     * @param extraClaims Additional claims.
     * @param userDetails The UserDetails object.
     * @param expiration The token expiration time in milliseconds.
     * @return The built JWT token string.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        // Explicitly setting the subject claim to the user's email address.
        // Assumes userDetails is an instance of your User entity which has getEmail().
        if (!(userDetails instanceof User)) {
            throw new IllegalArgumentException("UserDetails must be an instance of User entity");
        }
        User user = (User) userDetails;

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail()) // <-- Explicitly using getEmail()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a token is valid for a given user.
     * Compares the subject claim (extracted via extractUsername, which is email)
     * from the token with the user's email from UserDetails and checks if the token is expired.
     * @param token The JWT token.
     * @param userDetails The UserDetails object (your User entity).
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Extract the subject (email) using the existing method name
        final String subjectFromToken = extractUsername(token); // extractUsername is extracting the email

        // --- Add log here to see the extracted subject ---
        logger.info("JWT Validation: Subject extracted from token: '{}'", subjectFromToken);
        // -----------------------------------------------


        // Get the user's email from UserDetails.
        // Assumes userDetails is an instance of your User entity which has getEmail().
        if (!(userDetails instanceof User)) {
            throw new IllegalArgumentException("UserDetails must be an instance of User entity");
        }
        User user = (User) userDetails;
        final String userEmailFromDetails = user.getEmail(); // <-- Explicitly using getEmail()

        // --- Add log here to see the user's email from UserDetails ---
        logger.info("JWT Validation: User email from UserDetails: '{}'", userEmailFromDetails);
        // -----------------------------------------------------------


        // Compare the email from the token with the email from UserDetails and check expiration
        boolean emailsMatch = subjectFromToken.equals(userEmailFromDetails);
        boolean notExpired = !isTokenExpired(token);

        // --- Add log to show comparison results ---
        logger.info("JWT Validation: Emails match: {}, Token not expired: {}", emailsMatch, notExpired);
        // ----------------------------------------

        return emailsMatch && notExpired;
    }

    /**
     * Checks if the token has expired.
     * @param token The JWT token.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        boolean expired = expirationDate.before(new Date());
        // --- Add log for expiration check ---
        logger.info("JWT Validation: Token expiration date: {}, Current date: {}, Is Expired: {}", expirationDate, new Date(), expired);
        // ------------------------------------
        return expired;
    }

    /**
     * Extracts the expiration date claim from the JWT.
     * @param token The JWT token.
     * @return The expiration Date.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from the JWT.
     * @param token The JWT token.
     * @return The Claims object containing all payload data.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Gets the signing key from the secret key.
     * @return The signing Key.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
