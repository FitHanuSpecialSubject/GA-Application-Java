package org.fit.ssapp.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SmtRunConfigValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStableMatchingConfig {
    String message() default "Invalid StableMatchingProblemDto values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

