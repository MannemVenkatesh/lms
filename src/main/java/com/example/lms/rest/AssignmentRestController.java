package com.example.lms.rest;

import com.example.lms.entity.Assignment;
import com.example.lms.entity.Labour;
import com.example.lms.entity.Project;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.LabourRepository;
import com.example.lms.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentRestController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LabourRepository labourRepository;

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentRepository.findAll());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(assignmentRepository.findByProjectId(projectId));
    }

    @GetMapping("/labour/{labourId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByLabour(@PathVariable Long labourId) {
        return ResponseEntity.ok(assignmentRepository.findByLabourId(labourId));
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Labour labour = labourRepository.findById(request.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        // Check if labour is already assigned to this project
        boolean alreadyAssigned = assignmentRepository.existsByProjectIdAndLabourIdAndStatus(
                request.getProjectId(), request.getLabourId(), com.example.lms.entity.AssignmentStatus.ACTIVE);

        if (alreadyAssigned) {
            return ResponseEntity.status(409)
                    .body(new ErrorResponse("Labour is already assigned to this project"));
        }

        // Check if labour is currently occupied (ACTIVE assignment in any project)
        List<Assignment> activeAssignments = assignmentRepository.findByLabourId(labour.getId()).stream()
                .filter(a -> a.getStatus() == com.example.lms.entity.AssignmentStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());

        if (!activeAssignments.isEmpty()) {
            Assignment existingAssignment = activeAssignments.get(0);
            return ResponseEntity.status(409).body(new OccupiedErrorResponse(
                    "Labour is currently occupied with another project",
                    existingAssignment.getProject().getName(),
                    existingAssignment.getProject().getId(),
                    existingAssignment.getStartDate()));
        }

        Assignment assignment = new Assignment();
        assignment.setProject(project);
        assignment.setLabour(labour);
        assignment.setStartDate(LocalDate.now());
        assignment.setStatus(com.example.lms.entity.AssignmentStatus.ACTIVE);

        Assignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Assignment> completeAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(com.example.lms.entity.AssignmentStatus.COMPLETED);
        assignment.setEndDate(LocalDate.now());

        Assignment updated = assignmentRepository.save(assignment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Inner class for request body
    public static class AssignmentRequest {
        private Long projectId;
        private Long labourId;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Long getLabourId() {
            return labourId;
        }

        public void setLabourId(Long labourId) {
            this.labourId = labourId;
        }
    }

    // Error response classes
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class OccupiedErrorResponse extends ErrorResponse {
        private String occupiedProjectName;
        private Long occupiedProjectId;
        private LocalDate assignmentStartDate;

        public OccupiedErrorResponse(String message, String occupiedProjectName,
                Long occupiedProjectId, LocalDate assignmentStartDate) {
            super(message);
            this.occupiedProjectName = occupiedProjectName;
            this.occupiedProjectId = occupiedProjectId;
            this.assignmentStartDate = assignmentStartDate;
        }

        public String getOccupiedProjectName() {
            return occupiedProjectName;
        }

        public void setOccupiedProjectName(String occupiedProjectName) {
            this.occupiedProjectName = occupiedProjectName;
        }

        public Long getOccupiedProjectId() {
            return occupiedProjectId;
        }

        public void setOccupiedProjectId(Long occupiedProjectId) {
            this.occupiedProjectId = occupiedProjectId;
        }

        public LocalDate getAssignmentStartDate() {
            return assignmentStartDate;
        }

        public void setAssignmentStartDate(LocalDate assignmentStartDate) {
            this.assignmentStartDate = assignmentStartDate;
        }
    }
}
