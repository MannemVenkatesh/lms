package com.example.lms.repository;

import com.example.lms.entity.AvailabilityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityEventRepository extends JpaRepository<AvailabilityEvent, Long> {
    List<AvailabilityEvent> findByLabourId(Long labourId);
}
