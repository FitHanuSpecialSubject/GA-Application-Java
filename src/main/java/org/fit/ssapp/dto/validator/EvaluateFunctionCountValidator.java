package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * **EvaluateFunctionCountValidator** - Validator for evaluating function count in matching problems.
 * This validator ensures that the number of evaluation functions provided in the
 * `StableMatchingProblemDto` matches the expected number of sets in the problem.
 */
public class EvaluateFunctionCountValidator implements
        ConstraintValidator<ValidEvaluateFunctionCount, StableMatchingProblemDto> {

  /**
   * Initializes the validator.
   *
   * @param annotation The annotation instance for additional configurations (if needed).
   */
  @Override
  public void initialize(ValidEvaluateFunctionCount annotation) {
  }

  /**
   * Validates whether the number of evaluation functions matches the number of sets.
   *
   * @param dto     The `StableMatchingProblemDto` containing the evaluation functions and set count.
   * @param context The validation context.
   * @return `true` if the function count matches the number of sets, otherwise `false`.
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    return dto.getEvaluateFunctions().length == dto.getNumberOfSets();
  }
}
