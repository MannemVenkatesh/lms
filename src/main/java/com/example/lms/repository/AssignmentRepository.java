package com.example.lms.repository;

import com.example.lms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByProjectId(Long projectId);

    List<Assignment> findByLabourId(Long labourId);

    // Check if labour is already assigned to a specific project
    boolean existsByProjectIdAndLabourIdAndStatus(Long projectId, Long labourId,
            com.example.lms.entity.AssignmentStatus status);

    // Find active assignments for a labour
    List<Assignment> findByLabourIdAndStatus(Long labourId, com.example.lms.entity.AssignmentStatus status);
}
