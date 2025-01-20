package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidIndividualArraysSize.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndividualArraysSizeValidator.class)
public @interface ValidIndividualArraysSize {

  /**
   * message .
   *
   * @return default message.
   */
  String message() default "Individual arrays' size count (individualSetIndices, individualCapacities, individualRequirements, individualWeights, individualProperties) mismatch with number of individuals";

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
