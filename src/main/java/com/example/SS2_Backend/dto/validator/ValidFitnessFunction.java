package com.example.SS2_Backend.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FitnessFunctionValidator.class)
public @interface ValidFitnessFunction {
    String message() default "Invalid fitness function syntax";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
