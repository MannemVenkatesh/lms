package com.example.lms.controller;

import com.example.lms.entity.Assignment;
import com.example.lms.entity.Labour;
import com.example.lms.entity.Project;
import com.example.lms.entity.Skill;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.LabourRepository;
import com.example.lms.repository.SkillRepository;
import com.example.lms.service.ProjectService;
import com.example.lms.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private LabourRepository labourRepository;

    @Autowired
    private SkillRepository skillRepository;

    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "projects/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "projects/create";
    }

    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute Project project, BindingResult result) {
        if (result.hasErrors()) {
            return "projects/create";
        }
        projectService.saveProject(project);
        return "redirect:/projects";
    }

    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project Id:" + id));
        model.addAttribute("project", project);

        // Get recommendations based on required skills
        if (project.getRequiredSkills() != null && !project.getRequiredSkills().isEmpty()) {
            List<String> skillNames = project.getRequiredSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());
            List<Labour> recommendedLabours = recommendationService.recommendLabours(skillNames);
            model.addAttribute("recommendations", recommendedLabours);
        }

        model.addAttribute("allLabours", labourService.getAllLabours()); // For manual assignment dropdown if needed
        return "projects/view";
    }

    @Autowired
    private com.example.lms.service.LabourService labourService; // Inject LabourService

    @PostMapping("/{id}/skills")
    public String addSkill(@PathVariable Long id, @RequestParam String skillName) {
        Project project = projectService.getProjectById(id).orElseThrow();
        Skill skill = skillRepository.findByName(skillName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    return skillRepository.save(newSkill);
                });

        if (project.getRequiredSkills() == null) {
            project.setRequiredSkills(new HashSet<>());
        }
        project.getRequiredSkills().add(skill);
        projectService.saveProject(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignLabour(@PathVariable Long id, @RequestParam Long labourId) {
        Project project = projectService.getProjectById(id).orElseThrow();
        Labour labour = labourRepository.findById(labourId).orElseThrow();

        Assignment assignment = new Assignment();
        assignment.setProject(project);
        assignment.setLabour(labour);
        assignment.setStartDate(LocalDate.now());
        assignment.setStatus("ACTIVE");

        assignmentRepository.save(assignment);

        return "redirect:/projects/" + id;
    }
}
