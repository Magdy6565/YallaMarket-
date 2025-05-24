package com.example.demo;
import com.example.demo.dto.LoginUserDto;
import com.example.demo.dto.RegisterUserDto;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simple test cases for authentication (login and signup)
 * Fixed version without MessagingException issues
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationSimpleTestFixed {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;    @MockitoBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepository.deleteAll();
        
        // Reset mock behavior
        reset(authenticationManager);
    }

    // ======================== SIGNUP TESTS ========================

    @Test
    void testSignup_Success() {
        // Given
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole(2); // Retail Store
        registerDto.setAddress("123 Test Street");
        registerDto.setContactInfo("01234567890");

        // When
        User result = authenticationService.signup(registerDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(2, result.getRole());
        assertEquals("123 Test Street", result.getAddress());
        assertEquals("01234567890", result.getContactInfo());
        assertFalse(result.isEnabled()); // Should be disabled until verified
        assertNotNull(result.getVerificationCode()); // Should have verification code
        assertNotNull(result.getVerificationCodeExpiresAt()); // Should have expiry time

        // Verify user was saved to database
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("testuser", savedUser.get().getUsername());
    }

    @Test
    void testSignup_DuplicateEmail() {
        // Given - create a user first
        RegisterUserDto firstUser = new RegisterUserDto();
        firstUser.setUsername("user1");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setPassword("password123");
        firstUser.setRole(1); // Vendor
        firstUser.setAddress("Address 1");
        firstUser.setContactInfo("01111111111");

        authenticationService.signup(firstUser);

        // When - try to create another user with same email
        RegisterUserDto secondUser = new RegisterUserDto();
        secondUser.setUsername("user2");
        secondUser.setEmail("duplicate@example.com"); // Same email
        secondUser.setPassword("password456");
        secondUser.setRole(2);
        secondUser.setAddress("Address 2");
        secondUser.setContactInfo("02222222222");

        // Then
        assertThrows(DuplicateEmailException.class, () -> {
            authenticationService.signup(secondUser);
        });
    }

    @Test
    void testSignup_DuplicateUsername() {
        // Given - create a user first
        RegisterUserDto firstUser = new RegisterUserDto();
        firstUser.setUsername("duplicateuser");
        firstUser.setEmail("user1@example.com");
        firstUser.setPassword("password123");
        firstUser.setRole(1);
        firstUser.setAddress("Address 1");
        firstUser.setContactInfo("01111111111");

        authenticationService.signup(firstUser);

        // When - try to create another user with same username
        RegisterUserDto secondUser = new RegisterUserDto();
        secondUser.setUsername("duplicateuser"); // Same username
        secondUser.setEmail("user2@example.com");
        secondUser.setPassword("password456");
        secondUser.setRole(2);
        secondUser.setAddress("Address 2");
        secondUser.setContactInfo("02222222222");

        // Then
        assertThrows(DuplicateUsernameException.class, () -> {
            authenticationService.signup(secondUser);
        });
    }

    @Test
    void testSignup_EmptyFields() {
        // Given
        RegisterUserDto registerDto = new RegisterUserDto();
        // Leave fields empty to test validation

        // Then - should fail due to validation (handled by @Valid annotation in controller)
        // This test mainly verifies the service doesn't crash with empty data
        assertThrows(Exception.class, () -> {
            authenticationService.signup(registerDto);
        });
    }

    // ======================== LOGIN TESTS ========================

    @Test
    void testLogin_Success() {
        // Given - create and verify a user first
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("loginuser");
        registerDto.setEmail("login@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole(2);
        registerDto.setAddress("Login Address");
        registerDto.setContactInfo("01234567890");

        User user = authenticationService.signup(registerDto);
        
        // Manually enable the user (simulate successful verification)
        user.setEnabled(true);
        userRepository.save(user);

        // Mock successful authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("login@example.com", "password123"));

        // When
        LoginUserDto loginDto = new LoginUserDto();
        loginDto.setEmail("login@example.com");
        loginDto.setPassword("password123");

        User result = authenticationService.authenticate(loginDto);

        // Then
        assertNotNull(result);
        assertEquals("loginuser", result.getUsername());
        assertEquals("login@example.com", result.getEmail());
        assertTrue(result.isEnabled());

        // Verify authentication manager was called
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginUserDto loginDto = new LoginUserDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("password123");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(loginDto);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testLogin_UserNotVerified() {
        // Given - create a user but don't verify them
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("unverified");
        registerDto.setEmail("unverified@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole(1);
        registerDto.setAddress("Unverified Address");
        registerDto.setContactInfo("01234567890");

        authenticationService.signup(registerDto);

        // When
        LoginUserDto loginDto = new LoginUserDto();
        loginDto.setEmail("unverified@example.com");
        loginDto.setPassword("password123");

        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(loginDto);
        });

        assertEquals("Account not verified. Please verify your account.", exception.getMessage());
    }

    @Test
    void testLogin_WrongPassword() {
        // Given - create and verify a user
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("passwordtest");
        registerDto.setEmail("passwordtest@example.com");
        registerDto.setPassword("correctpassword");
        registerDto.setRole(2);
        registerDto.setAddress("Password Test Address");
        registerDto.setContactInfo("01234567890");

        User user = authenticationService.signup(registerDto);
        user.setEnabled(true);
        userRepository.save(user);

        // Mock failed authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When
        LoginUserDto loginDto = new LoginUserDto();
        loginDto.setEmail("passwordtest@example.com");
        loginDto.setPassword("wrongpassword");

        // Then
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(loginDto);
        });
    }

    // ======================== HELPER TESTS ========================

    @Test
    void testPasswordEncoding() {
        // Given
        String plainPassword = "testpassword123";
        
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("encryptiontest");
        registerDto.setEmail("encryption@example.com");
        registerDto.setPassword(plainPassword);
        registerDto.setRole(1);
        registerDto.setAddress("Encryption Address");
        registerDto.setContactInfo("01234567890");

        // When
        User user = authenticationService.signup(registerDto);

        // Then
        assertNotEquals(plainPassword, user.getPassword()); // Password should be encrypted
        assertTrue(passwordEncoder.matches(plainPassword, user.getPassword())); // But should match when decoded
    }

    @Test
    void testUserRepository_FindByEmail() {
        // Given
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("repotest");
        registerDto.setEmail("repotest@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole(2);
        registerDto.setAddress("Repo Test Address");
        registerDto.setContactInfo("01234567890");

        authenticationService.signup(registerDto);

        // When
        Optional<User> foundUser = userRepository.findByEmail("repotest@example.com");
        Optional<User> notFoundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("repotest", foundUser.get().getUsername());
        assertFalse(notFoundUser.isPresent());
    }

    @Test
    void testUserRepository_FindByUsername() {
        // Given
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("usernametest");
        registerDto.setEmail("usernametest@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole(1);
        registerDto.setAddress("Username Test Address");
        registerDto.setContactInfo("01234567890");

        authenticationService.signup(registerDto);

        // When
        Optional<User> foundUser = userRepository.findByUsername("usernametest");
        Optional<User> notFoundUser = userRepository.findByUsername("nonexistentuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("usernametest@example.com", foundUser.get().getEmail());
        assertFalse(notFoundUser.isPresent());
    }
}
