package com.crud.crud.repository;

import com.crud.crud.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long> {

    // Derived Queries
    List<Student> findByName(String name); // automatically translates to select * from student where name = ?
    List<Student> findByAge(int age); // automatically translates to select * from student where age = ?
    List<Student> findByNameAndAge(String name, int age); // automatically translates to select * from student where name = ? and age = ?

    //JPQL
    @Query("Select s from Student s where s.age > :age")
    List<Student> findStudentAboveAge(@Param("age") int age);

    // Native SQL
    @Query(value = "SELECT * FROM student WHERE name LIKE %:name%", nativeQuery = true)
    List<Student> searchByNameLike(@Param("name") String name);
}
