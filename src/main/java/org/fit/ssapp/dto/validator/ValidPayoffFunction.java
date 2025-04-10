package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidEvaluateFunction** - Annotation for validating evaluation function syntax.
 * This annotation ensures that evaluation functions follow the correct **mathematical syntax**
 * and contain only **allowed variables**. The validation is handled by `EvaluateFunctionValidator`.
 * ## **Validation Logic:**
 * - Checks if the function is syntactically valid.
 * - Ensures only permitted variables and mathematical operations are used.
 * - If the function is invalid, an error message is generated.
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FitnessValidateGT.class)
public @interface ValidPayoffFunction {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Invalid evaluate function syntax";

  /**
   * Defines validation groups (optional).
   *
   * @return An array of validation groups.
   */
  Class<?>[] groups() default {};

  /**
   * Specifies additional payload (optional).
   *
   * @return An array of payload classes.
   */
  Class<? extends Payload>[] payload() default {};
}