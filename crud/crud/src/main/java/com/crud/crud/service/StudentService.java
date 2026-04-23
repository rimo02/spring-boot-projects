package com.crud.crud.service;

import com.crud.crud.exception.ResourceNotFoundException;
import com.crud.crud.model.Student;
import com.crud.crud.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository repository){
        this.studentRepository=repository;
    }
    public Student create(Student student){
        return studentRepository.save(student);
    }
    public List<Student> getAll(){
        return studentRepository.findAll();
    }
//    public Optional<Student> getById(Long id){
//        return studentRepository.findById(id);
//    }
    public Student getById(Long id){
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
    public List<Student> getByName(String name){
        return studentRepository.findByName(name);
    }
    public List<Student> getByAge(int age) {
        if (age < 0) {
            throw new ResourceNotFoundException("Age cannot be negative");
        }
        return studentRepository.findByAge(age);
    }
    public List<Student> getByNameAndAge(String name, int age) {
        return studentRepository.findByNameAndAge(name, age);
    }

    public Student update(Long id, Student newStudent){
        Student student = studentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Cannot update. Student not found with id: " + id));
        student.setName(newStudent.getName());
        student.setAge(newStudent.getAge());
        return studentRepository.save(student);
    }
    public void delete(Long id) {
        if(!studentRepository.existsById(id)){
            throw new ResourceNotFoundException("Cannot delete. Student not found with id: " + id);
        };
        studentRepository.deleteById(id);
    }

    public List<Student> customJPQL(int age) {
        return studentRepository.findStudentAboveAge(age);
    }

    public List<Student> customNative(String name) {
        return studentRepository.searchByNameLike(name);
    }

}
