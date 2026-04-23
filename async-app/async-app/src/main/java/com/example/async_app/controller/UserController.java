package com.example.async_app.controller;

import com.example.async_app.dto.UserResponse;
import com.example.async_app.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public CompletableFuture<UserResponse> getUser(@PathVariable int id) {

        CompletableFuture<String> userFuture = userService.getUserData(id);
        CompletableFuture<String> orderFuture = userService.getUserOrders(id);

        return userFuture.thenCombine(orderFuture,
                (user, order) -> new UserResponse(user, order)
        );
    }
}