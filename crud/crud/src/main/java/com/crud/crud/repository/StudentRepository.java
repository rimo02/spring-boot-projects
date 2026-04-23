package com.crud.crud.repository;

import com.crud.crud.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findByName(String name); // automatically translates to select * from student where name = ?
    List<Student> findByAge(int age); // automatically translates to select * from student where age = ?
    List<Student> findByNameAndAge(String name, int age); // automatically translates to select * from student where name = ? and age = ?
}
