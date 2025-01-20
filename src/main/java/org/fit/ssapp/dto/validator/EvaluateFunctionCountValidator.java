package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * EvaluateFunctionCountValidator.
 */
public class EvaluateFunctionCountValidator implements
        ConstraintValidator<ValidEvaluateFunctionCount, StableMatchingProblemDto> {

  /**
   * ValidEvaluateFunctionCount initialize.
   */
  @Override
  public void initialize(ValidEvaluateFunctionCount annotation) {
  }

  /**
   * isValid .
   *
   * @return boolean
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    return dto.getEvaluateFunctions().length == dto.getNumberOfSets();
  }
}
