package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingPrDto;

public class IndividualArraysSizeValidator implements
    ConstraintValidator<ValidIndividualArraysSize, StableMatchingPrDto> {

  @Override
  public void initialize(ValidIndividualArraysSize annotation) {
  }

  @Override
  public boolean isValid(StableMatchingPrDto dto, ConstraintValidatorContext context) {
    int expectedCount = dto.getNumberOfIndividuals();
    return
        dto.getIndividualSetIndices().length == expectedCount &&
            dto.getIndividualCapacities().length == expectedCount &&
            dto.getIndividualRequirements().length == expectedCount &&
            dto.getIndividualWeights().length == expectedCount &&
            dto.getIndividualProperties().length == expectedCount;
  }
}
