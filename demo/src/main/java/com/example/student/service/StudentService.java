package com.example.student.service;

import com.example.student.dto.StudentDTO;
import com.example.student.entity.Student;
import com.example.student.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository repository;
    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }
    public Student create(StudentDTO dto){
        Student student = new Student(null, dto.getName(), dto.getEmail(), dto.getAge());
        return repository.save(student);
    }
    public List<Student> getAll(){
        return repository.findAll();
    }
    public Student getById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
    }
    public Student update(Long id, StudentDTO dto){
        Student student = getById(id);
        student.setAge(dto.getAge());
        student.setEmail(dto.getEmail());
        student.setName(dto.getName());
        return student;
    }
    public void delete(Long id){
        repository.deleteById(id);
    }
}
