package com.example.SS2_Backend.dto.validator;

import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EvaluateFunctionCountValidator implements ConstraintValidator<ValidEvaluateFunctionCount, StableMatchingProblemDTO> {

    @Override
    public void initialize(ValidEvaluateFunctionCount annotation) {}

    @Override
    public boolean isValid(StableMatchingProblemDTO dto, ConstraintValidatorContext context) {
        return dto.getEvaluateFunctions().length == dto.getNumberOfSets();
    }
}
