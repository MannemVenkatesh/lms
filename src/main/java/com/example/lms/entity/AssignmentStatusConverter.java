package com.example.lms.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssignmentStatusConverter implements AttributeConverter<AssignmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssignmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return AssignmentStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle legacy values mapping if necessary
            // Assuming Assignment status was uppercase "ACTIVE", "COMPLETED" mostly
            // But if it was mixed case, we handle here
            return AssignmentStatus.valueOf(dbData.toUpperCase());
        }
    }
}
