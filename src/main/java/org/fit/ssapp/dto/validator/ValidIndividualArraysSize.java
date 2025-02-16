package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidIndividualArraysSize** - Annotation for validating individual-related array sizes.
 * This annotation ensures that **all individual-related arrays** in `StableMatchingProblemDto`
 * have the correct number of individuals. The validation is handled by `IndividualArraysSizeValidator`.
 * ## **Validation Logic:**
 * - The following arrays must have a length matching `dto.getNumberOfIndividuals()`:
 *   - `dto.getIndividualSetIndices()`
 *   - `dto.getIndividualCapacities()`
 *   - `dto.getIndividualRequirements()`
 *   - `dto.getIndividualWeights()`
 *   - `dto.getIndividualProperties()`
 * - If any array has an incorrect length, validation **fails**.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndividualArraysSizeValidator.class)
public @interface ValidIndividualArraysSize {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Individual arrays' size count "
          + "(individualSetIndices, individualCapacities, "
          + "individualRequirements, individualWeights, individualProperties) "
          + "mismatch with number of individuals";

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
