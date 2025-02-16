package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidIndividualArrayPropertyCount** - Annotation for validating individual property array sizes.
 * This annotation ensures that **individual-related arrays** (requirements, weights, and properties)
 * in `StableMatchingProblemDto` have the correct number of properties.
 * The validation is handled by `IndividualArrayPropertyCountValidator`.
 * ## **Validation Logic:**
 * - The following arrays must have a length matching `dto.getNumberOfProperty()`:
 *   - `dto.getIndividualRequirements()`
 *   - `dto.getIndividualWeights()`
 *   - `dto.getIndividualProperties()`
 * - If any array has an incorrect length, validation **fails**.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndividualArrayPropertyCountValidator.class)
public @interface ValidIndividualArrayPropertyCount {

  /**
   * Specifies additional payload (optional).
   *
   * @return An array of payload classes.
   */
  String message() default
          "Individual array property count (requirements, weights, properties) "
                  + "mismatch with number of property";

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
