package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrategyStructureValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStrategyStructure {
    String message() default "Strategies and properties must be consistent across all normal players";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

