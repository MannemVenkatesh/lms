package com.example.lms.rest;

import com.example.lms.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityRestController {

    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleMissedCall(
            @RequestParam("Caller") String caller,
            @RequestParam("To") String to,
            @RequestParam(value = "CallSid", required = false) String callSid) {

        // Normalize phone numbers if necessary (e.g., remove +91 prefix)
        // For simplicity, we assume exact match or simple normalization

        availabilityService.handleMissedCall(caller, to, callSid);

        return ResponseEntity.ok("Received");
    }
}
