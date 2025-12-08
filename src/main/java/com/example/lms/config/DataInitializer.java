package com.example.lms.config;

import com.example.lms.entity.Labour;
import com.example.lms.entity.LabourStatus;
import com.example.lms.entity.Role;
import com.example.lms.entity.User;
import com.example.lms.repository.LabourRepository;
import com.example.lms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, LabourRepository labourRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create Super Admin if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.SUPER_ADMIN);
                userRepository.save(admin);
                System.out.println("Created default admin user: admin / admin123");
            }

            // Create Project Owner if not exists
            if (userRepository.findByUsername("owner").isEmpty()) {
                User owner = new User();
                owner.setUsername("owner");
                owner.setPassword(passwordEncoder.encode("owner123"));
                owner.setRole(Role.PROJECT_OWNER);
                userRepository.save(owner);
                System.out.println("Created default project owner: owner / owner123");
            }

            // Create Test Labour
            if (labourRepository.findByContactNumber("9876543210").isEmpty()) {
                Labour labour = new Labour();
                labour.setName("Test Labour");
                labour.setContactNumber("9876543210");
                labour.setHourlyRate(100.0);
                labour.setStatus(LabourStatus.AVAILABLE);
                labourRepository.save(labour);
                System.out.println("Created default labour: Test Labour / 9876543210");
            }
        };
    }
}
