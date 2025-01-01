package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

public class IndividualArrayPropertyCountValidator implements
    ConstraintValidator<ValidIndividualArrayPropertyCount, StableMatchingProblemDto> {

  @Override
  public void initialize(ValidIndividualArrayPropertyCount annotation) {
  }

  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    int expectedPropertyCount = dto.getNumberOfProperty();

    return Arrays.stream(dto.getIndividualRequirements())
        .allMatch(row -> row.length == expectedPropertyCount) &&
        Arrays.stream(dto.getIndividualWeights())
            .allMatch(row -> row.length == expectedPropertyCount) &&
        Arrays.stream(dto.getIndividualProperties())
            .allMatch(row -> row.length == expectedPropertyCount);
  }
}
