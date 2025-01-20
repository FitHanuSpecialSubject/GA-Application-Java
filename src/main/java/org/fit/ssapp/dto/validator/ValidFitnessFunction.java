package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidFitnessFunction.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FitnessFunctionValidator.class)
public @interface ValidFitnessFunction {

  /**
   * message .
   *
   * @return default message.
   */
  String message() default "Invalid fitness function syntax";

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
