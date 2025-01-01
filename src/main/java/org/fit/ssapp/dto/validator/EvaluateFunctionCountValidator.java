package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingPrDto;

public class EvaluateFunctionCountValidator implements
    ConstraintValidator<ValidEvaluateFunctionCount, StableMatchingPrDto> {

  @Override
  public void initialize(ValidEvaluateFunctionCount annotation) {
  }

  @Override
  public boolean isValid(StableMatchingPrDto dto, ConstraintValidatorContext context) {
    return dto.getEvaluateFunctions().length == dto.getNumberOfSets();
  }
}
