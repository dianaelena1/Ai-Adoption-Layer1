package com.msg.bank.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService.login() Unit Tests")
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountLockService lockService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, lockService);
    }

    @Test
    @DisplayName("Should return success AuthResult when username and password are valid")
    void testLoginWithValidCredentials() {
        // Arrange
        String username = "john.doe@msg.com";
        String password = "SecurePass123!";
        User user = new User(username, "john.doe@msg.com");
        user.setPasswordHash(hashPassword(password));
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(lockService.isAccountLocked(username)).thenReturn(false);

        // Act
        AuthResult result = authService.login(username, password);

        // Assert
        assertNotNull(result, "AuthResult should not be null");
        assertTrue(result.isSuccess(), "Login should succeed with valid credentials");
        assertEquals(username, result.getUsername(), "Returned username should match input");
        assertNotNull(result.getToken(), "Token should be generated on success");
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should return failure AuthResult when password is incorrect")
    void testLoginWithIncorrectPassword() {
        // Arrange
        String username = "john.doe@msg.com";
        String correctPassword = "SecurePass123!";
        String wrongPassword = "WrongPassword123!";
        User user = new User(username, "john.doe@msg.com");
        user.setPasswordHash(hashPassword(correctPassword));
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(lockService.isAccountLocked(username)).thenReturn(false);

        // Act
        AuthResult result = authService.login(username, wrongPassword);

        // Assert
        assertNotNull(result, "AuthResult should not be null");
        assertFalse(result.isSuccess(), "Login should fail with incorrect password");
        assertEquals("Invalid username or password", result.getErrorMessage(), 
                     "Error message should indicate invalid credentials");
        assertNull(result.getToken(), "No token should be generated on failure");
    }

    @Test
    @DisplayName("Should return locked account error when account is blocked")
    void testLoginWithLockedAccount() {
        // Arrange
        String username = "locked.user@msg.com";
        String password = "AnyPassword123!";
        User user = new User(username, "locked.user@msg.com");
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(lockService.isAccountLocked(username)).thenReturn(true);

        // Act
        AuthResult result = authService.login(username, password);

        // Assert
        assertNotNull(result, "AuthResult should not be null");
        assertFalse(result.isSuccess(), "Login should fail for locked account");
        assertEquals("Account is locked. Contact administrator.", result.getErrorMessage(),
                     "Error message should indicate account is locked");
        assertNull(result.getToken(), "No token should be generated for locked account");
        verify(lockService, times(1)).isAccountLocked(username);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when username is null")
    void testLoginWithNullUsername() {
        // Arrange
        String username = null;
        String password = "SecurePass123!";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                     () -> authService.login(username, password),
                     "Should throw IllegalArgumentException when username is null");
        
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is null")
    void testLoginWithNullPassword() {
        // Arrange
        String username = "john.doe@msg.com";
        String password = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                     () -> authService.login(username, password),
                     "Should throw IllegalArgumentException when password is null");
        
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should return failure when username does not exist")
    void testLoginWithNonExistentUsername() {
        // Arrange
        String username = "nonexistent@msg.com";
        String password = "SomePassword123!";
        
        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act
        AuthResult result = authService.login(username, password);

        // Assert
        assertNotNull(result, "AuthResult should not be null");
        assertFalse(result.isSuccess(), "Login should fail for non-existent user");
        assertEquals("Invalid username or password", result.getErrorMessage(),
                     "Error message should not reveal whether username exists");
        assertNull(result.getToken(), "No token should be generated");
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}
