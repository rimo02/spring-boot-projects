package com.example.async_app.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
