package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndividualArrayPropertyCountValidator.class)
public @interface ValidIndividualArrayPropertyCount {

  String message() default "Individual array property count (requirements, weights, properties) mismatch with number of property";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
