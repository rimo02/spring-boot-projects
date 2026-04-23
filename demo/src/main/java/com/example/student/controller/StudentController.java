package com.example.student.controller;

import com.example.student.dto.StudentDTO;
import com.example.student.entity.Student;
import com.example.student.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service){
        this.service = service;
    }

    @PostMapping
    public List<Student> getALl(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id){
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody StudentDTO dto){
        return service.update(id,dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Deleted successfully";
    }

}
