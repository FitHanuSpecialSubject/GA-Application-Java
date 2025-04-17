package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatrixDimensionValidator.class)
@Documented
public @interface ValidMatrixDimension {
    String message() default "Matrix dimensions are invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

