package com.example.demo.service;

import com.example.demo.dto.TaskDTO;
import com.example.demo.entity.Task;
import com.example.demo.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskService {
    private final TaskRepository repository;

    @Autowired
    public TaskService(TaskRepository repository){
        this.repository = repository;
    }

    @Transactional
    public Task Create(TaskDTO dto){
        Task task = new Task(null, dto.getTitle(),dto.getDescription(),"NEW");
        return repository.save(task);
    }
    public List<Task> getAll() {
        return repository.findAll();
    }
    public List<Task> search(String keyword){
        return repository.searchTasks(keyword);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<Task>> getTaskAsync(){
        List<Task> tasks = repository.findAll();
        return CompletableFuture.completedFuture(tasks);
    }
}
