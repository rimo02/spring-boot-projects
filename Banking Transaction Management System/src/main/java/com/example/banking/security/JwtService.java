package com.example.banking.security;

import com.example.banking.auth.entity.User;
import com.example.banking.auth.dto.AuthDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public JwtService(JwtEncoder encoder, @Value("${banking.jwt.issuer}") String issuer,
            @Value("${banking.jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public AuthDtos.Token issue(User user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .issuedAt(now).expiresAt(now.plus(ttl)).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new AuthDtos.Token(token, "Bearer", ttl.toSeconds(), user.getId());
    }
}
