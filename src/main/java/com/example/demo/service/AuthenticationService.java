package com.example.demo.service;

import com.example.demo.dto.LoginUserDto;
import com.example.demo.dto.RegisterUserDto;
import com.example.demo.dto.VerifyUserDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.DuplicateUsernameException; // Import if you add this
import com.example.demo.exception.DuplicateContactInfoException; // Import if you add this
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.math.BigDecimal; // Import BigDecimal for rating


@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            // Changed from RuntimeException to DuplicateEmailException
            throw new DuplicateEmailException("The email address '" + input.getEmail() + "' is already registered.");
        }

        // Add similar checks for username and contactInfo if they are unique
        if (userRepository.findByUsername(input.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("The username '" + input.getUsername() + "' is already taken.");
        }
        if (userRepository.findByContactInfo(input.getContactInfo()).isPresent()) {
            throw new DuplicateContactInfoException("The contact information '" + input.getContactInfo() + "' is already in use.");
        }

        User user = new User(); // Use the default constructor
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        // Set the new fields from the input DTO
        user.setAddress(input.getAddress());
        user.setContactInfo(input.getContactInfo());
        String rawVerificationCode = generateVerificationCode();
        String hashedVerificationCode = passwordEncoder.encode(rawVerificationCode);
        user.setVerificationCode(hashedVerificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        user.setRating(BigDecimal.ZERO);
        user.setDeletedAt(null);
        user.setRole(input.getRole());
        sendVerificationEmail(user , rawVerificationCode);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if (optionalUser.isEmpty()) {
            // User not found by email
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();

        // Check if the user is already enabled (optional, but good practice)
        if (user.isEnabled()) {
            // User is already verified, perhaps return a specific status or message
            // For now, we'll just let it proceed or you could throw a different exception
            System.out.println("User with email " + input.getEmail() + " is already verified.");
            return; // Or throw new RuntimeException("User already verified");
        }


        // Check if the verification code has expired
        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            // Code has expired
            // You might want to trigger sending a new code here instead of just throwing
            throw new RuntimeException("Verification code has expired");
        }

        // --- Correct comparison using PasswordEncoder.matches() ---

        // Get the raw code submitted by the user from the DTO
        String submittedRawCode = input.getVerificationCode();

        // Get the hashed code stored in the database
        String storedHashedCode = user.getVerificationCode();

        // Use passwordEncoder.matches() to compare the raw submitted code with the stored hash
        // This method handles hashing the submittedRawCode and comparing it securely
        boolean codeMatches = passwordEncoder.matches(submittedRawCode, storedHashedCode);


        if (codeMatches) {
            // If the codes match and haven't expired, enable the user
            user.setEnabled(true);
            user.setVerificationCode(null); // Clear the verification code after successful verification
            user.setVerificationCodeExpiresAt(null); // Clear the expiry time
            userRepository.save(user); // Save the updated user state

            // Verification successful - method completes without throwing
        } else {
            // If the codes don't match
            // You might want to log failed attempts here
            throw new RuntimeException("Invalid verification code");
        }
    }


    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
//            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            String rawVerificationCode = generateVerificationCode();
            String hashedVerificationCode = passwordEncoder.encode(rawVerificationCode);
            user.setVerificationCode(hashedVerificationCode);
            sendVerificationEmail(user , rawVerificationCode);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendVerificationEmail(User user , String vcode) { //TODO: Update with company logo
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + vcode;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}