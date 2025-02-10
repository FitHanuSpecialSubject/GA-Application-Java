package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * IndividualArraysSizeValidator.
 */
public class IndividualArraysSizeValidator implements
        ConstraintValidator<ValidIndividualArraysSize, StableMatchingProblemDto> {

  /**
   * ValidIndividualArraysSize initialize.
   */
  @Override
  public void initialize(ValidIndividualArraysSize annotation) {
  }

  /**
   * isValid .
   *
   * @return boolean.
   *
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    int expectedCount = dto.getNumberOfIndividuals();
    return
            dto.getIndividualSetIndices().length == expectedCount
                    &&
                    dto.getIndividualCapacities().length == expectedCount
                    &&
                    dto.getIndividualRequirements().length == expectedCount
                    &&
                    dto.getIndividualWeights().length == expectedCount
                    &&
                    dto.getIndividualProperties().length == expectedCount;
  }
}
