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
    boolean isValid = true;
    int expectedPropertyCount = dto.getNumberOfProperty();
    context.disableDefaultConstraintViolation();

    // Validate individualRequirements
    if (dto.getIndividualRequirements() != null) {
      for (int i = 0; i < dto.getIndividualRequirements().length; i++) {
        if (dto.getIndividualRequirements()[i] != null && dto.getIndividualRequirements()[i].length != expectedPropertyCount) {
          context.buildConstraintViolationWithTemplate(
                          String.format("individualRequirements[%d].length must be %d", i, expectedPropertyCount))
                  .addPropertyNode("individualRequirements")
                  .addConstraintViolation();
          isValid = false;
        }
      }
    }

    // Validate individualWeights
    if (dto.getIndividualWeights() != null) {
      for (int i = 0; i < dto.getIndividualWeights().length; i++) {
        if (dto.getIndividualWeights()[i] != null && dto.getIndividualWeights()[i].length != expectedPropertyCount) {
          context.buildConstraintViolationWithTemplate(
                          String.format("individualWeights[%d].length must be %d", i, expectedPropertyCount))
                  .addPropertyNode("individualWeights")
                  .addConstraintViolation();
          isValid = false;
        }
      }
    }

    // Validate individualProperties
    if (dto.getIndividualProperties() != null) {
      for (int i = 0; i < dto.getIndividualProperties().length; i++) {
        if (dto.getIndividualProperties()[i] != null && dto.getIndividualProperties()[i].length != expectedPropertyCount) {
          context.buildConstraintViolationWithTemplate(
                          String.format("individualProperties[%d].length must be %d", i, expectedPropertyCount))
                  .addPropertyNode("individualProperties")
                  .addConstraintViolation();
          isValid = false;
        }
      }
    }

    return isValid;
  }
}
