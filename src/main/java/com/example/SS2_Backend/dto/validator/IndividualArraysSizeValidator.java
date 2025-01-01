package com.example.SS2_Backend.dto.validator;

import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IndividualArraysSizeValidator implements ConstraintValidator<ValidIndividualArraysSize, StableMatchingProblemDTO> {

    @Override
    public void initialize(ValidIndividualArraysSize annotation) {}

    @Override
    public boolean isValid(StableMatchingProblemDTO dto, ConstraintValidatorContext context) {
        int expectedCount = dto.getNumberOfIndividuals();
        return
                dto.getIndividualSetIndices().length == expectedCount &&
                dto.getIndividualCapacities().length == expectedCount &&
                dto.getIndividualRequirements().length == expectedCount &&
                dto.getIndividualWeights().length == expectedCount &&
                dto.getIndividualProperties().length == expectedCount;
    }
}
