package com.msg.bank.auth;

/**
 * Authentication Service - handles user login logic
 */
public class AuthService {
    private UserRepository userRepository;
    private AccountLockService lockService;

    public AuthService(UserRepository userRepository, AccountLockService lockService) {
        this.userRepository = userRepository;
        this.lockService = lockService;
    }

    /**
     * Authenticates user with username and password
     *
     * @param username user's username
     * @param password user's password (plaintext)
     * @return AuthResult with success status and token or error message
     * @throws IllegalArgumentException if username or password is null
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (lockService.isAccountLocked(username)) {
            return AuthResult.failure("Account is locked. Contact administrator.");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return AuthResult.failure("Invalid username or password");
        }

        String passwordHash = hashPassword(password);
        if (!user.verifyPassword(password, user.getPasswordHash())) {
            return AuthResult.failure("Invalid username or password");
        }

        String token = generateToken(username);
        return AuthResult.success(username, token);
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    private String generateToken(String username) {
        return "token_" + System.currentTimeMillis() + "_" + username.hashCode();
    }
}
