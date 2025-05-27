package com.microvolunteer.repository;

import com.microvolunteer.entity.TaskImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskImageRepository extends JpaRepository<TaskImage, Long> {

    List<TaskImage> findByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);
}