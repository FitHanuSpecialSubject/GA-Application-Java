package com.example.SS2_Backend.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvaluateFunctionValidator.class)
public @interface ValidEvaluateFunction {
    String message() default "Invalid evaluate function syntax";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}