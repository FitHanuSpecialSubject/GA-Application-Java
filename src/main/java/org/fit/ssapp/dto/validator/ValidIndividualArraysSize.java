package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndividualArraysSizeValidator.class)
public @interface ValidIndividualArraysSize {

  String message() default "Individual arrays' size count (individualSetIndices, individualCapacities, individualRequirements, individualWeights, individualProperties) mismatch with number of individuals";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
