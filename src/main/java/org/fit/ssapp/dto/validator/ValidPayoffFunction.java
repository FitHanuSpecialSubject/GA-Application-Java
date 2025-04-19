package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidPayoffFunction** - Annotation for validating payoff function syntax.
 * This annotation ensures that payoff functions follow the correct **mathematical syntax**
 * and contain only **allowed variables**. The validation is handled by `PayoffValidator`.
 * ## **Validation Logic:**
 * - Checks if the function is syntactically valid.
 * - Ensures only permitted variables and mathematical operations are used.
 * - Validates against mathematical errors like division by zero, sqrt of negative numbers, etc.
 * - Ensures functions have the correct number of arguments.
 * - If the function is invalid, a detailed error message is generated.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PayoffValidator.class)
public @interface ValidPayoffFunction {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Invalid payoff function syntax or mathematical error";

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