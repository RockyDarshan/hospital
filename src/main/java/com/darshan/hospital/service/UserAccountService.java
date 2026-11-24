package com.darshan.hospital.service;

import com.darshan.hospital.entity.Role;
import com.darshan.hospital.entity.User;
import com.darshan.hospital.exception.DuplicateResourceException;
import com.darshan.hospital.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createAccount(String email, String rawPassword, Role role) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalized)) {
            throw new DuplicateResourceException("Email already registered: " + normalized);
        }
        return users.save(new User(normalized, passwordEncoder.encode(rawPassword), role));
    }
}