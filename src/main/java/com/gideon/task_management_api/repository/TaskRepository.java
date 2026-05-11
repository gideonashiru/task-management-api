package com.gideon.task_management_api.repository;

import com.gideon.task_management_api.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByTitle(String title);

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);
}
