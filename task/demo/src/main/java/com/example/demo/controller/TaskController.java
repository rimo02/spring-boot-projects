package com.example.demo.controller;

import com.example.demo.dto.TaskDTO;
import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {

    @Autowired
    private TaskService service;

    @PostMapping
    public Task Create(@RequestBody TaskDTO dto){
        return service.Create(dto);
    }
    @GetMapping
    public List<Task> getAll() {
        return service.getAll();
    }
    @GetMapping("/search")
    public List<Task> search(@RequestParam String keyword){
        return service.search(keyword);
    }
}
