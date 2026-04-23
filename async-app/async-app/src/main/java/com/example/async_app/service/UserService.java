package com.example.async_app.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Async("taskExecutor")
    public CompletableFuture<String> getUserData(int id) {
        simulateDelay();
        return CompletableFuture.completedFuture("User data for id = " + id);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getUserOrders(int id) {
        simulateDelay();
        return CompletableFuture.completedFuture("Orders for User ID: " + id);
    }

    private void simulateDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}