package com.example.lms.service;

import com.example.lms.entity.AvailabilityEvent;
import com.example.lms.entity.Labour;
import com.example.lms.entity.LabourStatus;
import com.example.lms.repository.AvailabilityEventRepository;
import com.example.lms.repository.LabourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AvailabilityService {

    @Autowired
    private LabourRepository labourRepository;

    @Autowired
    private AvailabilityEventRepository availabilityEventRepository;

    // Dedicated numbers for availability (Mock values)
    private static final String AVAILABLE_NUMBER = "9000000001";
    private static final String BUSY_NUMBER = "9000000002";

    public void handleMissedCall(String callerNumber, String calledNumber, String callId) {
        // 1. Identify Worker
        Optional<Labour> labourOpt = labourRepository.findByContactNumber(callerNumber);
        if (labourOpt.isEmpty()) {
            System.out.println("Unknown caller: " + callerNumber);
            return; // Or log error
        }

        Labour labour = labourOpt.get();

        // 2. Determine Intent
        String eventType;
        LabourStatus newStatus;

        if (AVAILABLE_NUMBER.equals(calledNumber)) {
            eventType = "Available";
            newStatus = LabourStatus.AVAILABLE;
        } else if (BUSY_NUMBER.equals(calledNumber)) {
            eventType = "Busy";
            newStatus = LabourStatus.UNAVAILABLE;
        } else {
            System.out.println("Unknown called number: " + calledNumber);
            return;
        }

        // 3. Log Event
        AvailabilityEvent event = new AvailabilityEvent();
        event.setLabour(labour);
        event.setTimestamp(LocalDateTime.now());
        event.setEventType(eventType);
        event.setCallId(callId);
        availabilityEventRepository.save(event);

        // 4. Update Status
        labour.setStatus(newStatus);
        labourRepository.save(labour);

        System.out.println("Updated labour " + labour.getName() + " status to " + newStatus);
    }
}
