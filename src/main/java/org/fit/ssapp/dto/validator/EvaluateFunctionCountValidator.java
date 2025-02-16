package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * Validator class for ensuring that the number of evaluate functions in a
 * {@link StableMatchingProblemDto} matches the specified number of sets.
 */
public class EvaluateFunctionCountValidator implements
    ConstraintValidator<ValidEvaluateFunctionCount, StableMatchingProblemDto> {

  /**
   * @param annotation annotation instance for a given constraint declaration
   */
  @Override
  public void initialize(ValidEvaluateFunctionCount annotation) {
  }

  /**
   * Validates whether the number of evaluate functions in the provided
   * {@link StableMatchingProblemDto} matches the specified number of sets.
   *
   * @param dto     the {@link StableMatchingProblemDto} to validate
   * @param context the context in which the constraint is evaluated
   * @return true if the number of evaluate functions matches the number of sets, false otherwise
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    return dto.getEvaluateFunctions().length == dto.getNumberOfSets();
  }
}
