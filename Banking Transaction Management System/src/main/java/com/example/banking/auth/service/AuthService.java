package com.example.banking.auth.service;

import com.example.banking.auth.dto.AuthDtos;
import com.example.banking.auth.entity.User;
import com.example.banking.auth.repository.UserRepository;
import com.example.banking.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Transactional
    public AuthDtos.Token register(AuthDtos.Register request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password too long");
        User user = users.saveAndFlush(new User(normalize(request.email()), passwords.encode(request.password())));
        return jwt.issue(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.Token login(AuthDtos.Login request) {
        var user = users.findByEmail(normalize(request.email()));
        // Always do BCrypt work, including for an unknown email, to reduce timing
        // disclosure.
        String hash = user.map(User::getPasswordHash).orElse(DUMMY_HASH);
        boolean matches = passwords.matches(request.password(), hash);
        if (!matches || user.isEmpty())
            throw new BadCredentialsException("Invalid credentials");
        return jwt.issue(user.get());
    }

    private static final String DUMMY_HASH = new BCryptPasswordEncoder(12).encode("dummy-password-never-used");

    private String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
