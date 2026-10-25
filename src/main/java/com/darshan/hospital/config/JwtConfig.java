package com.darshan.hospital.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private SecretKey secretKey(JwtProperties props) {
        return new SecretKeySpec(
                props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // Signs new tokens (used at login).
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(props)));
    }

    // Verifies the signature, expiry and issuer of incoming tokens.
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey(props))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer()));
        return decoder;
    }
}