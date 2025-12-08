package com.example.lms.repository;

import com.example.lms.entity.Labour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabourRepository extends JpaRepository<Labour, Long> {
    Optional<Labour> findByContactNumber(String contactNumber);
}
