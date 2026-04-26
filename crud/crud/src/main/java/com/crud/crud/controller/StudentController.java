package com.crud.crud.controller;

import com.crud.crud.dto.StudentRequest;
import com.crud.crud.dto.StudentResponse;
import com.crud.crud.model.Student;
import com.crud.crud.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    @Autowired
    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@RequestBody StudentRequest request){
        Student student = new Student();
        student.setName(request.getName());
        student.setAge(request.getAge());
        student.setPassword(request.getPassword());

        Student saved = service.create(student);
        StudentResponse res = new StudentResponse();
        res.setId(saved.getId());
        res.setName(saved.getName());
        res.setAge(saved.getAge());

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAll(){
        List<StudentResponse> students = service.getAll().stream().map(this::convertToResponse).toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping
    public ResponseEntity<Page<Student>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String keyword
    ){
        return ResponseEntity.ok(service.getStudents(page,size,sortBy,keyword));
    }

    @GetMapping("/{id}")
    public Student getStudentByid(Long id){
        return service.getById(id);
    }

    @GetMapping("/search") // /students/search?name=Rimo&age=20
    public List<Student> filter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer age){
        if (name != null && age != null) {
            return service.getByNameAndAge(name, age);
        } else if (name != null) {
            return service.getByName(name);
        } else if (age != null) {
            return service.getByAge(age);
        } else {
            return service.getAll();
        }
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student student){
        return service.update(id,student);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    private StudentResponse convertToResponse(Student student) {
        StudentResponse res = new StudentResponse();
        res.setId(student.getId());
        res.setName(student.getName());
        res.setAge(student.getAge());
        return res;
    }

    // JPQL
    @GetMapping("/custom/jpql")
    public List<StudentResponse> customJPQL(@RequestParam int age) {
        return service.customJPQL(age).stream().map(this::convert).toList();
    }

    // Native SQL
    @GetMapping("/custom/native")
    public List<StudentResponse> customNative(@RequestParam String name) {
        return service.customNative(name).stream().map(this::convert).toList();
    }
    private StudentResponse convert(Student student) {
        StudentResponse res = new StudentResponse();
        res.setId(student.getId());
        res.setName(student.getName());
        res.setAge(student.getAge());
        return res;
    }

}
