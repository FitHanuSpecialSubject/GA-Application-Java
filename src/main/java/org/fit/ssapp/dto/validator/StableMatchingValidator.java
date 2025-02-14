package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StableMatchingValidator implements ConstraintValidator<ValidStableMatching, StableMatchingProblemDto> {

    private final ValidationConfig validationConfig;

    @Autowired
    public StableMatchingValidator(ValidationConfig validationConfig) {
        this.validationConfig = validationConfig;
    }

    @Override
    public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (dto.getPopulationSize() > validationConfig.getMaxPopulation()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Population size must not exceed " + validationConfig.getMaxPopulation())
                    .addPropertyNode("populationSize")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getGeneration() > validationConfig.getMaxGeneration()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Generation must not exceed " + validationConfig.getMaxGeneration())
                    .addPropertyNode("generation")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getNumberOfIndividuals() < validationConfig.getMinIndividuals()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Number of individuals must be at least " + validationConfig.getMinIndividuals())
                    .addPropertyNode("numberOfIndividuals")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}


