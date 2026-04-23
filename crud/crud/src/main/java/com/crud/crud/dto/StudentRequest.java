package com.crud.crud.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public class StudentRequest {
    private String name;
    private int age;
    private String password;
}
