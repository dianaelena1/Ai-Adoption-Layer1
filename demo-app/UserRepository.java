package com.msg.bank.auth;

/**
 * Service interface for user repository
 */
public interface UserRepository {
    User findByUsername(String username);
    void save(User user);
    void delete(String username);
}
