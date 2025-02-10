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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequirementSyntaxValidator.class)
public @interface ValidRequirementSyntax {

  /**
   * message .
   *
   * @return default message.
   */
  String message() default "Invalid individual requirement syntax";

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