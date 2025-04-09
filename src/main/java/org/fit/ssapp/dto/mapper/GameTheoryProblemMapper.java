package org.fit.ssapp.dto.mapper;

import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.PsoCompatibleGameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.StandardGameTheoryProblem;
import org.fit.ssapp.util.EvaluatorUtils;
import org.fit.ssapp.util.StringUtils;
import org.fit.ssapp.util.StringExpressionEvaluator;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper class for converting a GameTheoryProblemDto request into a GameTheoryProblem object. This
 * class helps in mapping data from DTO format to the appropriate game theory problem type.
 */
public class GameTheoryProblemMapper {

  private static final Pattern VARIABLE_PATTERN =  Pattern.compile("(u[1-9]\\d*|M\\d+|S\\d+|SIGMA\\{[^}]+\\}|P\\d+p\\d+)");
  private static final String[] VALID_FUNCTIONS = {"AVERAGE", "SUM", "MIN", "MAX", "PRODUCT", "MEDIAN", "RANGE"};

  /**
   * Converts a GameTheoryProblemDto request to a GameTheoryProblem object. Depending on the
   * algorithm type, it returns either a {@link PsoCompatibleGameTheoryProblem} or a
   * {@link StandardGameTheoryProblem}.
   *
   * @param request The DTO containing game theory problem details.
   * @return A mapped GameTheoryProblem object.
   */
  public static GameTheoryProblem toProblem(GameTheoryProblemDto request) {
    GameTheoryProblem problem;
    String algorithm = request.getAlgorithm();
    if (!StringUtils.isEmptyOrNull(algorithm)
        && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
      problem = new PsoCompatibleGameTheoryProblem();
    } else {
      problem = new StandardGameTheoryProblem();
    }

    // Validate payoff function
    String payoffFunction = request.getDefaultPayoffFunction();
    if (payoffFunction != null && !payoffFunction.equalsIgnoreCase("DEFAULT")) {
      if (!StringExpressionEvaluator.validatePayoffFunction(payoffFunction)) {
        throw new IllegalArgumentException("Invalid payoff function: " + payoffFunction);
      }
    }
    
    problem.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(payoffFunction));
    String fitnessFunction = request.getFitnessFunction();
    if (fitnessFunction == null || fitnessFunction.equalsIgnoreCase("DEFAULT")) {
      fitnessFunction = "AVERAGE";
    }
    // Validate fitness function
    if (!isValidFitnessFunction(fitnessFunction)) {
      throw new IllegalArgumentException("Invalid fitness function: " + fitnessFunction);
    }
    problem.setFitnessFunction(fitnessFunction);
    problem.setSpecialPlayer(request.getSpecialPlayer());
    problem.setNormalPlayers(request.getNormalPlayers());
    problem.setConflictSet(request.getConflictSet());
    problem.setMaximizing(request.isMaximizing());

    return problem;
  }

  private static boolean isValidFitnessFunction(String function) {
    if (function == null) return false;
    for (String valid : VALID_FUNCTIONS) {
      if (valid.equalsIgnoreCase(function)) {
        return true;
      }
    }
    
    // check mathematical expression
    try {
      String cleanFunc = function.replaceAll("\\s+", "");
      Matcher matcher = VARIABLE_PATTERN.matcher(cleanFunc);
      Set<String> variables = new HashSet<>();
      while (matcher.find()) {
        variables.add(matcher.group(0));
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
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Converts a StandardGameTheoryProblem to a PsoCompatibleGameTheoryProblem.
   *
   * @param problem The standard game theory problem to convert.
   * @return A PsoCompatibleGameTheoryProblem instance with copied data.
   */
  public static PsoCompatibleGameTheoryProblem toPsoProblem(StandardGameTheoryProblem problem) {
    PsoCompatibleGameTheoryProblem result = new PsoCompatibleGameTheoryProblem();
    result.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(problem.getDefaultPayoffFunction()));
    result.setFitnessFunction(EvaluatorUtils
        .getValidFitnessFunction(problem.getFitnessFunction()));
    result.setSpecialPlayer(problem.getSpecialPlayer());
    result.setNormalPlayers(problem.getNormalPlayers());
    result.setConflictSet(problem.getConflictSet());
    result.setMaximizing(problem.isMaximizing());
    return result;
  }

  /**
   * Converts a PsoCompatibleGameTheoryProblem to a StandardGameTheoryProblem.
   *
   * @param problem The PSO-compatible game theory problem to convert.
   * @return A StandardGameTheoryProblem instance with copied data.
   */
  public static StandardGameTheoryProblem toStandardProblem(
      PsoCompatibleGameTheoryProblem problem) {
    StandardGameTheoryProblem result = new StandardGameTheoryProblem();
    result.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(problem.getDefaultPayoffFunction()));
    result.setFitnessFunction(EvaluatorUtils
        .getValidFitnessFunction(problem.getFitnessFunction()));
    result.setSpecialPlayer(problem.getSpecialPlayer());
    result.setNormalPlayers(problem.getNormalPlayers());
    result.setConflictSet(problem.getConflictSet());
    result.setMaximizing(problem.isMaximizing());
    return result;
  }
}
