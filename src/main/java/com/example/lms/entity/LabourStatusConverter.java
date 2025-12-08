package com.example.lms.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LabourStatusConverter implements AttributeConverter<LabourStatus, String> {

    @Override
    public String convertToDatabaseColumn(LabourStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public LabourStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return LabourStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle legacy values mapping
            if ("Busy".equalsIgnoreCase(dbData)) {
                return LabourStatus.UNAVAILABLE;
            }
            if ("Available".equalsIgnoreCase(dbData)) {
                return LabourStatus.AVAILABLE;
            }
            throw e;
        }
    }
}
