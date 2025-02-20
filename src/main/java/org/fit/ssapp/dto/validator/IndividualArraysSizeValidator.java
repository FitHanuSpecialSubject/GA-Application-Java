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
   * Initializes the validator.
   *
   * @param annotation The annotation instance for additional configurations (if needed).
   */
  @Override
  public void initialize(ValidIndividualArraysSize annotation) {
  }

  /**
   * Validates whether all individual-related arrays have the correct size.
   *
   * @param dto     The `StableMatchingProblemDto` containing the arrays to validate.
   * @param context The validation context.
   * @return `true` if all arrays match the expected number of individuals, otherwise `false`.
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
