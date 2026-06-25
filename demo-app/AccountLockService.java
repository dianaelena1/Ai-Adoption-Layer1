package com.msg.bank.auth;

/**
 * Service interface for managing account locks
 */
public interface AccountLockService {
    boolean isAccountLocked(String username);
    void lockAccount(String username, String reason);
    void unlockAccount(String username);
}
