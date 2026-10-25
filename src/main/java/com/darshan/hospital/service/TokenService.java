package com.darshan.hospital.service;

import com.darshan.hospital.config.JwtProperties;
import com.darshan.hospital.entity.User;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    public record IssuedToken(String value, long expiresInSeconds) {
    }

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public TokenService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(props.expirationMinutes()));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedToken(token, Duration.between(now, expiresAt).toSeconds());
    }
}