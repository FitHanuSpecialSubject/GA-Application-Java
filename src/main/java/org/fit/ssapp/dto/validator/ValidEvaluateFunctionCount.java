package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidDistributedCores.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvaluateFunctionCountValidator.class)
public @interface ValidEvaluateFunctionCount {

  /**
   * message .
   *
   * @return default message.
   */
  String message() default "Evaluate functions count mismatch with number of sets";

  /**
   * groups .
   *
   * @return .
   */
  Class<?>[] groups() default {};

  /**
   * payload .
   *
   * @return .
   */
  Class<? extends Payload>[] payload() default {};
}
