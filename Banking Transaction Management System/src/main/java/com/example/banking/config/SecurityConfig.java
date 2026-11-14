package com.example.banking.config;

import com.example.banking.auth.repository.UserRepository;
import com.example.banking.security.JwtAuthenticationFilter;
import com.example.banking.common.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecretKeySpec jwtKey(@Value("${banking.jwt.secret}") String value) {
        byte[] bytes = Base64.getDecoder().decode(value);
        if (bytes.length < 32)
            throw new IllegalArgumentException("JWT_SECRET must be base64 encoding of at least 32 random bytes");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder encoder(SecretKeySpec key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder decoder(SecretKeySpec key, @Value("${banking.jwt.issuer}") String issuer) {
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationEntryPoint entryPoint(ObjectMapper json) {
        return (req, res, e) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            json.writeValue(res.getOutputStream(),
                    ApiError.of(401, "UNAUTHENTICATED", "Authentication required", req.getRequestURI()));
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtDecoder decoder, UserRepository users,
            AuthenticationEntryPoint entryPoint, ObjectMapper json) throws Exception {
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable()).httpBasic(b -> b.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/health")
                        .permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403);
                    res.setContentType("application/json");
                    json.writeValue(res.getOutputStream(),
                            ApiError.of(403, "ACCESS_DENIED", "Access denied", req.getRequestURI()));
                }))
                .addFilterBefore(new JwtAuthenticationFilter(decoder, users, entryPoint),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
