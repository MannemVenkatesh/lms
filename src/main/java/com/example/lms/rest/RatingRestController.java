package com.example.lms.rest;

import com.example.lms.entity.Labour;
import com.example.lms.entity.Project;
import com.example.lms.entity.Rating;
import com.example.lms.repository.LabourRepository;
import com.example.lms.repository.ProjectRepository;
import com.example.lms.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingRestController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private LabourRepository labourRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingRepository.findAll());
    }

    @GetMapping("/labour/{labourId}")
    public ResponseEntity<List<Rating>> getRatingsByLabour(@PathVariable Long labourId) {
        return ResponseEntity.ok(ratingRepository.findByLabourId(labourId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Rating>> getRatingsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ratingRepository.findByProjectId(projectId));
    }

    @GetMapping("/labour/{labourId}/average")
    public ResponseEntity<Double> getAverageRatingForLabour(@PathVariable Long labourId) {
        List<Rating> ratings = ratingRepository.findByLabourId(labourId);
        if (ratings.isEmpty()) {
            return ResponseEntity.ok(0.0);
        }
        double average = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
        return ResponseEntity.ok(Math.round(average * 10.0) / 10.0);
    }

    @PostMapping
    public ResponseEntity<?> createRating(@RequestBody RatingRequest request) {
        Labour labour = labourRepository.findById(request.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Validate score
        if (request.getScore() < 1 || request.getScore() > 5) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Score must be between 1 and 5"));
        }

        Rating rating = new Rating();
        rating.setLabour(labour);
        rating.setProject(project);
        rating.setScore(request.getScore());
        rating.setComments(request.getComments());

        try {
            Rating saved = ratingRepository.save(rating);
            return ResponseEntity.ok(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Rating already exists for this project and labour"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Inner classes
    public static class RatingRequest {
        private Long labourId;
        private Long projectId;
        private Integer score;
        private String comments;

        public Long getLabourId() {
            return labourId;
        }

        public void setLabourId(Long labourId) {
            this.labourId = labourId;
        }

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }

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
}
