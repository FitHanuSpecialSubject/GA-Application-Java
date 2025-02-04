package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * IndividualArrayPropertyCountValidator.
 */
public class IndividualArrayPropertyCountValidator implements
        ConstraintValidator<ValidIndividualArrayPropertyCount, StableMatchingProblemDto> {

  /**
   * ValidIndividualArrayPropertyCount initialize.
   */
  @Override
  public void initialize(ValidIndividualArrayPropertyCount annotation) {
  }

  /**
   * isValid .
   *
   * @return boolean
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    int expectedPropertyCount = dto.getNumberOfProperty();

    return Arrays.stream(dto.getIndividualRequirements())
            .allMatch(row -> row.length == expectedPropertyCount)
            &&
            Arrays.stream(dto.getIndividualWeights())
                    .allMatch(row -> row.length == expectedPropertyCount)
            &&
            Arrays.stream(dto.getIndividualProperties())
                    .allMatch(row -> row.length == expectedPropertyCount);
  }
}
