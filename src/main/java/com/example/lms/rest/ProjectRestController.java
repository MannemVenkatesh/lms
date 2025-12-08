package com.example.lms.rest;

import com.example.lms.entity.Project;
import com.example.lms.entity.Labour;
import com.example.lms.service.ProjectService;
import com.example.lms.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private com.example.lms.service.ProjectCostService projectCostService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project saved = projectService.saveProject(project);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        Project updated = projectService.saveProject(project);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<Labour>> getRecommendations(@PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<String> skillNames = java.util.Collections.emptyList();
        if (project.getRequiredSkills() != null && !project.getRequiredSkills().isEmpty()) {
            skillNames = project.getRequiredSkills().stream()
                    .map(skill -> skill.getName())
                    .collect(Collectors.toList());
        }

        List<Labour> recommendations = recommendationService.recommendLabours(skillNames);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<Project> addSkill(@PathVariable Long id, @RequestParam String skillName) {
        projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // We need to handle skill creation/retrieval here or in service
        // For simplicity, let's assume we can do it here or delegate to service if it
        // had the method
        // But ProjectService doesn't have addSkill exposed easily, so let's do it here
        // like ProjectController

        // Note: We need SkillRepository injected
        // Ideally this logic belongs in ProjectService

        projectService.addSkillToProject(id, skillName);

        return ResponseEntity.ok(projectService.getProjectById(id).get());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Project> updateProjectStatus(@PathVariable Long id,
            @RequestParam com.example.lms.entity.ProjectStatus status) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setStatus(status);
        Project updated = projectService.saveProject(project);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/cost-summary")
    public ResponseEntity<java.util.Map<String, Object>> getProjectCostSummary(@PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        java.util.Map<String, Object> costSummary = projectCostService.calculateProjectCost(id);

        if (project.getBudget() != null) {
            java.util.Map<String, Object> budgetAnalysis = projectCostService.getBudgetAnalysis(id,
                    project.getBudget());
            costSummary.putAll(budgetAnalysis);
        }

        return ResponseEntity.ok(costSummary);
    }
}
