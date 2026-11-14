package com.example.banking.auth.controller;

import com.example.banking.auth.dto.AuthDtos;
import com.example.banking.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.Token register(@Valid @RequestBody AuthDtos.Register request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.Token login(@Valid @RequestBody AuthDtos.Login request) {
        return service.login(request);
    }
}
