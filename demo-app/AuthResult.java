package com.msg.bank.auth;

/**
 * Result object returned by login operation
 */
public class AuthResult {
    private boolean success;
    private String username;
    private String token;
    private String errorMessage;

    public AuthResult(boolean success, String username, String token, String errorMessage) {
        this.success = success;
        this.username = username;
        this.token = token;
        this.errorMessage = errorMessage;
    }

    public static AuthResult success(String username, String token) {
        return new AuthResult(true, username, token, null);
    }

    public static AuthResult failure(String errorMessage) {
        return new AuthResult(false, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
