package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidEvaluateFunctionCount** - Annotation for validating the number of evaluation functions.
 * This annotation ensures that the number of evaluation functions provided in
 * `StableMatchingProblemDto` matches the expected number of sets. The validation is handled
 * by `EvaluateFunctionCountValidator`.
 * ## **Validation Logic:**
 * - The number of evaluation functions (`evaluateFunctions.length`) must be **equal to**
 *   the number of sets (`numberOfSets`).
 * - If they do not match, validation **fails**.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvaluateFunctionCountValidator.class)
public @interface ValidEvaluateFunctionCount {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Evaluate functions count mismatch with number of sets";

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
