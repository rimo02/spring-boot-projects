package com.example.demo.repository;

import com.example.demo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("Select t from Task t where t.status = :status")
    List<Task> findByStatus(@Param("status") String status);

    @Query(value = "Select * from Task where title like %:keyword%",nativeQuery = true)
    List<Task> searchTasks(@Param("keyword") String keyword);
}
