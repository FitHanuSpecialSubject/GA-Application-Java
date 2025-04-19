package org.fit.ssapp.dto.validator;

import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GameTheoryValidator implements ConstraintValidator<ValidGameTheory, GameTheoryProblemDto> {
    @Autowired
    private ValidationConfig validationConfig;

    @Override
    public boolean isValid(GameTheoryProblemDto dto, ConstraintValidatorContext context) {
        boolean isValid = true;
        isValid = new PopulationAndGenerationThreshold().validateThreshold(dto, context, validationConfig);
        return isValid;
    }
    
}
