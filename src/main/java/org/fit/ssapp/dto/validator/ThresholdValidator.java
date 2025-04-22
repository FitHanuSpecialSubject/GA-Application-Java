package org.fit.ssapp.dto.validator;

import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.dto.request.ProblemRequestDto;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ThresholdValidator implements ConstraintValidator<ValidThreshold, ProblemRequestDto> {
    @Autowired
    private ValidationConfig validationConfig;

    public boolean isValid(ProblemRequestDto dto, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (dto.getPopulationSize() > validationConfig.getPopulationSize()) {
            context.disableDefaultConstraintViolation();
            // Violation for the 'generation' field
            context.buildConstraintViolationWithTemplate(
                    "populationSize must not exceed " + validationConfig.getPopulationSize())
                    .addPropertyNode("populationSize")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getGeneration() > validationConfig.getGeneration()) {
            context.disableDefaultConstraintViolation();
            // Violation for the 'populationSize' field
            context.buildConstraintViolationWithTemplate(
                    "generation must not exceed " + validationConfig.getGeneration())
                    .addPropertyNode("generation")
                    .addConstraintViolation();
            isValid = false;

        }
        return isValid;
    }

}
