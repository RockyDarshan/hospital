package com.darshan.hospital.service;

import com.darshan.hospital.dto.LoginRequest;
import com.darshan.hospital.dto.LoginResponse;
import com.darshan.hospital.entity.User;
import com.darshan.hospital.exception.InvalidCredentialsException;
import com.darshan.hospital.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<User> found = users.findByEmail(email);

        // Always run one BCrypt check, even for an unknown email, so the response
        // time doesn't reveal whether an account exists.
        String hash = found.map(User::getPasswordHash).orElse(dummyHash);
        boolean passwordOk = passwordEncoder.matches(request.password(), hash);

        // One generic error for every failure, so attackers learn nothing.
        if (found.isEmpty() || !passwordOk || !found.get().isEnabled()) {
            throw new InvalidCredentialsException();
        }

        User user = found.get();
        TokenService.IssuedToken token = tokens.issue(user);
        return new LoginResponse(
                token.value(), "Bearer", token.expiresInSeconds(), user.getRole().name());
    }
}