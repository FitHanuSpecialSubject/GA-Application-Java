package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequirementSyntaxValidator.class)
public @interface ValidRequirementSyntax {

  String message() default "Invalid individual requirement syntax";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}