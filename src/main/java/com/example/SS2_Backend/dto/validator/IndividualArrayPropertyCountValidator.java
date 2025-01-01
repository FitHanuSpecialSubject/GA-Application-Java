package com.example.SS2_Backend.dto.validator;

import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class IndividualArrayPropertyCountValidator implements ConstraintValidator<ValidIndividualArrayPropertyCount, StableMatchingProblemDTO> {

    @Override
    public void initialize(ValidIndividualArrayPropertyCount annotation) {}

    @Override
    public boolean isValid(StableMatchingProblemDTO dto, ConstraintValidatorContext context) {
        int expectedPropertyCount = dto.getNumberOfProperty();

        return Arrays.stream(dto.getIndividualRequirements()).allMatch(row -> row.length == expectedPropertyCount) &&
                Arrays.stream(dto.getIndividualWeights()).allMatch(row -> row.length == expectedPropertyCount) &&
                Arrays.stream(dto.getIndividualProperties()).allMatch(row -> row.length == expectedPropertyCount);
    }
}
