package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * **ValidDistributedCores** - Annotation for validating distributed core allocation.
 * This annotation ensures that the specified number of CPU core is valid for distributed computing.
 * The validation is handled by **`DistributedCoresValidator`**, which checks that:
 * - The value is **"all"** (case-insensitive), allowing all available cores to be used.
 * - The value is a **positive integer** that does not exceed the system's available cores.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistributedCoresValidator.class)
public @interface ValidDistributedCores {

  /**
   * The default error message when validation fails.
   *
   * @return A string containing the default error message.
   */
  String message() default "Invalid distributed cores value";

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
