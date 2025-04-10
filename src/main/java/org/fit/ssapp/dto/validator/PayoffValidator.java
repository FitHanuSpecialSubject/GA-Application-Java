package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.GameTheoryConst;


public class PayoffValidator implements ConstraintValidator<ValidPayoffFunction, String> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?p[0-9]+");


  @Override
  public void initialize(ValidPayoffFunction constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value.equalsIgnoreCase (GameTheoryConst.DEFAULT_PAYOFF_FUNC) || value.isBlank()) {
      return true;
    }

        String temp = value.replaceAll("P([0-9]+)p([0-9]+)", "1");
        temp = temp.replaceAll("p([0-9]+)", "1");
        System.out.println("temp: " + temp);

        Expression builder = new ExpressionBuilder(temp).build();
        
      return builder.validate().isValid();

  }

}