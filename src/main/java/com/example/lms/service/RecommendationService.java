package com.example.lms.service;

import com.example.lms.entity.Labour;
import com.example.lms.entity.Rating;
import com.example.lms.entity.Skill;
import com.example.lms.repository.LabourRepository;
import com.example.lms.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private LabourRepository labourRepository;

    @Autowired
    private RatingRepository ratingRepository;

    public List<Labour> recommendLabours(List<String> requiredSkills) {
        List<Labour> allLabours = labourRepository.findAll();

        // Filter by skills and availability
        List<Labour> qualifiedLabours = allLabours.stream()
                .filter(labour -> labour.getStatus() == com.example.lms.entity.LabourStatus.AVAILABLE) // Check
                                                                                                       // availability
                .filter(labour -> {
                    if (labour.getSkills() == null) {
                        return false;
                    }
                    Set<String> labourSkillNames = labour.getSkills().stream()
                            .map(Skill::getName)
                            .collect(Collectors.toSet());
                    return labourSkillNames.containsAll(requiredSkills);
                })
                .collect(Collectors.toList());

        // Sort by average rating
        qualifiedLabours.sort((l1, l2) -> {
            Double r1 = getAverageRating(l1.getId());
            Double r2 = getAverageRating(l2.getId());
            return r2.compareTo(r1); // Descending
        });

        return qualifiedLabours;
    }

    private Double getAverageRating(Long labourId) {
        List<Rating> ratings = ratingRepository.findByLabourId(labourId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
    }
}
