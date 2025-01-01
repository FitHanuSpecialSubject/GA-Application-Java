package com.example.SS2_Backend.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvaluateFunctionCountValidator.class)
public @interface ValidEvaluateFunctionCount {
    String message() default "Evaluate functions count mismatch with number of sets"; ;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
