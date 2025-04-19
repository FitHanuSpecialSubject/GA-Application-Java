package org.fit.ssapp.dto.validator;

import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.dto.request.ProblemRequestDto;

import jakarta.validation.ConstraintValidatorContext;

public class PopulationAndGenerationThreshold {
    public boolean validateThreshold(ProblemRequestDto dto, ConstraintValidatorContext context, ValidationConfig validationConfig) {
        boolean isValid = true;
        int threshold = validationConfig.getThreshold();

        if (dto.getPopulationSize() * dto.getGeneration() > threshold) {
            context.disableDefaultConstraintViolation();
            // Violation for the 'generation' field
            context.buildConstraintViolationWithTemplate("Threshold (generation * populationSize) must not exceed " + threshold)
                    .addPropertyNode("generation")
                    .addConstraintViolation();

            // Violation for the 'populationSize' field
            context.buildConstraintViolationWithTemplate("Threshold (generation * populationSize) must not exceed " + threshold)
                    .addPropertyNode("populationSize")
                    .addConstraintViolation();

            isValid = false;    
        }
        return isValid;
    }
}
