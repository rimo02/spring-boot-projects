package com.crud.crud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message="Name not found")
    private String name;

    @Email(message = "Email is required")
    private int age;

    private String password;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Loan> loansList;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Book> books;

    public Student(){

    }
}
