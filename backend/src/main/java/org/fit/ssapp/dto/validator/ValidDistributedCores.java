package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistributedCoresValidator.class)
public @interface ValidDistributedCores {

  String message() default "Invalid distributed cores value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
