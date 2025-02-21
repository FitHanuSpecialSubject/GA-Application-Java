package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * **IndividualArrayPropertyCountValidator** - Validator for property count consistency.
 * This class ensures that all **individual-related arrays** in `StableMatchingProblemDto`
 * have the correct number of properties based on `dto.getNumberOfProperty()`.

 */
public class IndividualArrayPropertyCountValidator implements
        ConstraintValidator<ValidIndividualArrayPropertyCount, StableMatchingProblemDto> {

  /**
   * Initializes the validator.
   *
   * @param annotation The annotation instance for additional configurations (if needed).
   */
  @Override
  public void initialize(ValidIndividualArrayPropertyCount annotation) {
  }

  /**
   * Validates whether all individual-related arrays match the expected number of properties.
   *
   * @param dto     The `StableMatchingProblemDto` containing the arrays to validate.
   * @param context The validation context.
   * @return `true` if all arrays have the correct property count, otherwise `false`.
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
