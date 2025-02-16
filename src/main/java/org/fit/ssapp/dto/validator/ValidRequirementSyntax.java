package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidRequirementSyntax** - Annotation for validating individual requirement syntax.
 * This annotation ensures that individual requirement expressions are correctly formatted.
 * The validation is handled by `RequirementSyntaxValidator`.
 * ## **Validation Logic:**
 * - The requirement expressions must follow the expected format.
 * - Uses regex matching to verify valid syntax.
 * - If the syntax is incorrect, validation **fails**.
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequirementSyntaxValidator.class)
public @interface ValidRequirementSyntax {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Invalid individual requirement syntax";

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