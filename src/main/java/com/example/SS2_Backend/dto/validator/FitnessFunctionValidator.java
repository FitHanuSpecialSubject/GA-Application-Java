package com.example.SS2_Backend.dto.validator;

import com.example.SS2_Backend.constants.MatchingConst;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FitnessFunctionValidator implements ConstraintValidator<ValidFitnessFunction, String> {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(M\\d+|S\\d+|SIGMA\\{[^}]+\\})");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value.equalsIgnoreCase(MatchingConst.DEFAULT_EVALUATE_FUNC)) return true;
        String cleanFunc = value.replaceAll("\\s+", "");
        try {
            Set<String> variables = extractVariables(cleanFunc);

            for (String var : variables) {
                if (var.startsWith("SIGMA{") && var.endsWith("}")) {
                    cleanFunc = cleanFunc.replace(var, var.substring(6, var.length() - 1));
                }
            }

            ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
            for (String var : variables) {
                String cleanVar = var.startsWith("SIGMA{") && var.endsWith("}")
                        ? var.substring(6, var.length() - 1)
                        : var;
                builder.variable(cleanVar);
            }

            Expression expression = builder.build();

            for (String var : variables) {
                String cleanVar = var.startsWith("SIGMA{") && var.endsWith("}")
                        ? var.substring(6, var.length() - 1)
                        : var;
                expression.setVariable(cleanVar, 1.0);
            }

            expression.evaluate();
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid fitness function syntax: '" + value + "'")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private Set<String> extractVariables(String func) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(func);
        while (matcher.find()) variables.add(matcher.group(0));
        return variables;
    }
}