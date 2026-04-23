package com.example.async_app.dto;

import lombok.Data;

@Data
public class UserResponse {

    private String userData;
    private String orders;

    public UserResponse() {}

    public UserResponse(String userData, String orders) {
        this.userData = userData;
        this.orders = orders;
    }
}