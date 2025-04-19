package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GameTheoryValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGameTheory {
    String message() default "Invalid GameTheoryProblemDto values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

