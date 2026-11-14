package com.example.banking.security;

import com.example.banking.auth.entity.User;
import java.util.UUID;

public record Actor(UUID id, User.Role role) {
    public boolean admin() {
        return role == User.Role.ADMIN;
    }
}
