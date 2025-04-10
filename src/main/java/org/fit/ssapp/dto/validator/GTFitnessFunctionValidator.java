package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.GameTheoryConst;

public class GTFitnessFunctionValidator implements ConstraintValidator<ValidFitnessFunctionGT, String> {

    @Override
    public void initialize(ValidFitnessFunctionGT constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value.equalsIgnoreCase(GameTheoryConst.DEFAULT_PAYOFF_FUNC) || value.isBlank()) {
            return true;
        }

        List<String> functions = new ArrayList<>(6);
        functions.add("PRODUCT");
        functions.add("MAX");
        functions.add("MIN");
        functions.add("AVERAGE");
        functions.add("MEDIAN");
        functions.add("RANGE");


        return functions.contains(value);
    }

}
