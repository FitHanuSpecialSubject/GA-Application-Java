package org.fit.ssapp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import net.objecthunter.exp4j.function.Function;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.request.functioncheck.GtFunctionCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmEvaluateCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmFitnessCheckRequest;
import org.fit.ssapp.dto.response.functioncheck.FunctionCheckResult;
import org.fit.ssapp.dto.response.functioncheck.GtFunctionCheckResponse;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.fit.ssapp.ss.smt.requirement.Requirement;
import org.fit.ssapp.ss.smt.requirement.RequirementDecoder;
import org.fit.ssapp.util.EvaluatorUtils;
import org.fit.ssapp.util.PreferenceProviderUtils;
import org.fit.ssapp.util.StringExpressionEvaluator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.Permutation;
import org.springframework.stereotype.Service;

/**
 * Evaluates payoff / evaluate / fitness functions on given data <b>without running MOEA</b>.
 * <p>
 * The numbers are produced by the same production code paths the solvers use
 * ({@link StringExpressionEvaluator}, {@link TwoSetFitnessEvaluator},
 * {@link TwoSetPreferenceProvider}, the problem classes). Next to each value a
 * <i>reference value</i> is computed with plain exp4j variable binding so that bugs in the
 * production evaluation (e.g. {@code replaceAll("u1")} corrupting {@code u10}) show up as a
 * {@code mismatch}.
 */
@Slf4j
@Service
public class FunctionCheckService {

  private static final Pattern GT_PAYOFF_VARIABLE = Pattern.compile("(P[0-9]+)?p[0-9]+");
  private static final Pattern GT_FITNESS_VARIABLE = Pattern.compile("u[0-9]+");
  private static final double TOLERANCE = 1e-6;
  private static final String MISMATCH_HINT =
      "Production result differs from the reference computation. Likely a variable "
          + "substitution bug (e.g. 'u1' replacing the prefix of 'u10').";

  private static final Function LOGB = new Function("logb", 2) {
    @Override
    public double apply(double... args) {
      if (args[0] <= 0 || args[1] <= 0) {
        throw new IllegalArgumentException("Logarithm base and argument must be positive");
      }
      return Math.log(args[1]) / Math.log(args[0]);
    }
  };

  // ---------------------------------------------------------------------------------------
  // Game theory
  // ---------------------------------------------------------------------------------------

  /**
   * Check payoff functions of the chosen strategy profile, then the fitness function.
   *
   * @param request request
   * @return response
   */
  public GtFunctionCheckResponse checkGameTheory(GtFunctionCheckRequest request) {
    GtFunctionCheckResponse response = new GtFunctionCheckResponse();
    List<NormalPlayer> players = request.getNormalPlayers();
    double[] payoffs;
    boolean payoffsOk = true;
    String failedPayoffNote = null;

    if (players != null && !players.isEmpty()) {
      int[] chosen = resolveChosenIndices(request.getChosenStrategyIndices(), players);
      response.setChosenStrategyIndices(chosen);
      String defaultPayoff = EvaluatorUtils
          .getIfDefaultFunction(nullToEmpty(request.getDefaultPayoffFunction()));
      payoffs = new double[players.size()];
      for (int i = 0; i < players.size(); i++) {
        FunctionCheckResult r = checkPayoff(players, chosen, i, defaultPayoff);
        response.getPlayers().add(r);
        if (r.isSuccess()) {
          payoffs[i] = r.getValue();
        } else {
          payoffsOk = false;
          if (failedPayoffNote == null) {
            failedPayoffNote = "Payoff of player " + (i + 1) + " failed, fitness not computed.";
          }
        }
      }
    } else if (request.getPayoffs() != null && request.getPayoffs().length > 0) {
      payoffs = request.getPayoffs();
    } else {
      throw new IllegalArgumentException("Provide either normalPlayers or payoffs.");
    }

    for (double p : payoffs) {
      response.getPayoffs().add(p);
    }

    FunctionCheckResult fitness;
    if (payoffsOk) {
      fitness = checkGtFitness(payoffs, request.getFitnessFunction());
    } else {
      fitness = new FunctionCheckResult();
      fitness.setFunction(request.getFitnessFunction());
      fitness.setError(failedPayoffNote);
    }
    response.setFitness(fitness);
    if (fitness.isSuccess()) {
      response.setObjective(request.isMaximizing() ? -fitness.getValue() : fitness.getValue());
    }
    return response;
  }

  private FunctionCheckResult checkGtFitness(double[] payoffs, String fitnessFunction) {
    String fn = EvaluatorUtils.getValidFitnessFunction(nullToEmpty(fitnessFunction));
    FunctionCheckResult r = new FunctionCheckResult();
    r.setFunction(fn);
    r.setSyntaxValid(true);
    Double value = null;
    try {
      value = StringExpressionEvaluator.evaluateFitnessValue(payoffs, fn).doubleValue();
      r.setSuccess(true);
      r.setValue(value);
    } catch (Exception e) {
      r.setError(describe(e));
    }

    if (!isDefaultKeyword(fn)) {
      Set<String> names = findVariables(fn, GT_FITNESS_VARIABLE);
      r.setRequiredVariables(new ArrayList<>(names));
      try {
        Map<String, Double> values = new LinkedHashMap<>();
        for (String name : names) {
          int idx = Integer.parseInt(name.substring(1)) - 1;
          if (idx < 0 || idx >= payoffs.length) {
            throw new IllegalArgumentException(
                name + " refers to a player that does not exist (" + payoffs.length
                    + " players)");
          }
          values.put(name, payoffs[idx]);
        }
        r.setVariablesUsed(values);
        applyReference(r, value, referenceEvaluate(fn, names, values));
      } catch (Exception e) {
        r.setSyntaxValid(false);
        r.getNotes().add("Reference computation failed: " + describe(e));
      }
    }
    return r;
  }

  private FunctionCheckResult checkPayoff(List<NormalPlayer> players, int[] chosen, int i,
      String defaultPayoff) {
    FunctionCheckResult r = new FunctionCheckResult();
    NormalPlayer player = players.get(i);
    String fn = player.getPayoffFunction() == null ? defaultPayoff : player.getPayoffFunction();
    r.setFunction(fn);
    r.setSyntaxValid(true);
    r.getDetails().put("playerIndex", i + 1);
    r.getDetails().put("playerName", player.getName());
    r.getDetails().put("chosenStrategyIndex", chosen[i]);

    Strategy strategy;
    try {
      strategy = player.getStrategyAt(chosen[i]);
      r.getDetails().put("strategyName", strategy.getName());
      r.getDetails().put("strategyProperties", strategy.getProperties());
    } catch (Exception e) {
      r.setError("Cannot read chosen strategy: " + describe(e));
      return r;
    }

    Double value = null;
    try {
      BigDecimal v = fn.contains("P")
          ? StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
              strategy, fn, players, chosen)
          : StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(strategy, fn);
      value = v.doubleValue();
      r.setSuccess(true);
      r.setValue(value);
    } catch (Exception e) {
      r.setError(describe(e));
    }

    if (!fn.isBlank() && !isDefaultKeyword(fn)) {
      Set<String> names = findVariables(fn, GT_PAYOFF_VARIABLE);
      r.setRequiredVariables(new ArrayList<>(names));
      try {
        Map<String, Double> values = new LinkedHashMap<>();
        for (String name : names) {
          values.put(name, resolvePayoffVariable(name, strategy, players, chosen));
        }
        r.setVariablesUsed(values);
        applyReference(r, value, referenceEvaluate(fn, names, values));
      } catch (Exception e) {
        r.setSyntaxValid(false);
        r.getNotes().add("Reference computation failed: " + describe(e));
      }
    }
    return r;
  }

  private double resolvePayoffVariable(String name, Strategy own, List<NormalPlayer> players,
      int[] chosen) {
    if (name.startsWith("P")) {
      String[] parts = name.substring(1).split("p");
      int player = Integer.parseInt(parts[0]) - 1;
      int prop = Integer.parseInt(parts[1]) - 1;
      return players.get(player).getStrategyAt(chosen[player]).getProperties().get(prop);
    }
    return own.getProperties().get(Integer.parseInt(name.substring(1)) - 1);
  }

  private int[] resolveChosenIndices(int[] chosen, List<NormalPlayer> players) {
    if (chosen == null || chosen.length == 0) {
      return new int[players.size()];
    }
    if (chosen.length != players.size()) {
      throw new IllegalArgumentException("chosenStrategyIndices has " + chosen.length
          + " entries but there are " + players.size() + " players.");
    }
    for (int i = 0; i < chosen.length; i++) {
      List<Strategy> strategies = players.get(i).getStrategies();
      if (strategies == null || chosen[i] < 0 || chosen[i] >= strategies.size()) {
        throw new IllegalArgumentException(
            "chosenStrategyIndices[" + i + "]=" + chosen[i] + " is out of range.");
      }
    }
    return chosen;
  }

  // ---------------------------------------------------------------------------------------
  // Stable matching - evaluate function (P / W / R variables)
  // ---------------------------------------------------------------------------------------

  /**
   * Check a stable matching evaluate function.
   *
   * @param request request
   * @return result
   */
  public FunctionCheckResult checkSmEvaluate(SmEvaluateCheckRequest request) {
    String fn = EvaluatorUtils.getValidEvaluationFunction(nullToEmpty(request.getFunction()));
    FunctionCheckResult r = new FunctionCheckResult();
    r.setFunction(fn);
    if (fn.isEmpty()) {
      r.setError("Default evaluate function has no formula to test.");
      return r;
    }

    Set<String> names;
    Expression expression;
    try {
      names = PreferenceProviderUtils.getVariables(fn);
      expression = new ExpressionBuilder(fn).variables(names).build();
      r.setRequiredVariables(new ArrayList<>(names));
      for (String name : names) {
        expression.setVariable(name, 1.0);
      }
      ValidationResult validation = expression.validate();
      if (!validation.isValid()) {
        r.setError("Invalid syntax: " + validation.getErrors());
        return r;
      }
      r.setSyntaxValid(true);
    } catch (Exception e) {
      r.setError("Invalid syntax: " + describe(e));
      return r;
    }

    try {
      if (request.getProblem() != null) {
        evaluateFromDataset(request, fn, r);
      } else if (request.getVariables() != null) {
        Map<String, Double> values = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (String name : names) {
          Double v = request.getVariables().get(name);
          if (v == null) {
            missing.add(name);
          } else {
            values.put(name, v);
          }
        }
        if (!missing.isEmpty()) {
          r.setError("Missing values for variables: " + missing);
          return r;
        }
        expression.setVariables(values);
        r.setVariablesUsed(values);
        r.setValue(expression.evaluate());
        r.setSuccess(true);
      } else {
        r.getNotes().add("Syntax is valid. Provide variables or problem data to compute a value.");
      }
    } catch (Exception e) {
      r.setSuccess(false);
      r.setError(describe(e));
    }
    return r;
  }

  private void evaluateFromDataset(SmEvaluateCheckRequest request, String fn,
      FunctionCheckResult r) {
    StableMatchingProblemDto dto = request.getProblem();
    Integer evaluator = request.getEvaluatorIndex();
    Integer evaluated = request.getEvaluatedIndex();
    if (evaluator == null || evaluated == null) {
      throw new IllegalArgumentException("evaluatorIndex and evaluatedIndex are required.");
    }
    MatchingData data = buildMatchingData(dto);
    checkIndex(evaluator, data.getSize(), "evaluatorIndex");
    checkIndex(evaluated, data.getSize(), "evaluatedIndex");
    int set = data.getSetNoOf(evaluator);
    if (set != 0 && set != 1) {
      throw new IllegalArgumentException("Only two-set problems are supported (set=" + set + ").");
    }
    if (data.getSetNoOf(evaluated) == set) {
      throw new IllegalArgumentException(
          "evaluatedIndex must belong to the other set than evaluatorIndex.");
    }

    String[] functions = dto.getEvaluateFunctions() == null
        ? new String[] {"", ""}
        : Arrays.copyOf(dto.getEvaluateFunctions(), 2);
    for (int i = 0; i < 2; i++) {
      if (functions[i] == null) {
        functions[i] = "";
      }
    }
    functions[set] = fn;

    TwoSetPreferenceProvider provider = new TwoSetPreferenceProvider(data, functions);
    Map<String, Double> values = set == 0
        ? provider.getVariableValuesForSet1(evaluator, evaluated)
        : provider.getVariableValuesForSet2(evaluator, evaluated);
    Expression expression = set == 0 ? provider.getExpressionOfSet1()
        : provider.getExpressionOfSet2();
    expression.setVariables(values);
    r.setVariablesUsed(new LinkedHashMap<>(values));
    r.setValue(expression.evaluate());
    r.setSuccess(true);
    r.getDetails().put("evaluatorSet", set);
    r.getDetails().put("evaluatorIndex", evaluator);
    r.getDetails().put("evaluatedIndex", evaluated);
  }

  // ---------------------------------------------------------------------------------------
  // Stable matching - fitness function (M / S / SIGMA)
  // ---------------------------------------------------------------------------------------

  /**
   * Check a stable matching fitness function.
   *
   * @param request request
   * @return result
   */
  public FunctionCheckResult checkSmFitness(SmFitnessCheckRequest request) {
    FunctionCheckResult r = new FunctionCheckResult();
    String fn = EvaluatorUtils.getValidFitnessFunction(nullToEmpty(request.getFunction()));
    r.setFunction(fn);
    try {
      if (request.getProblem() != null) {
        fitnessFromDataset(request, fn, r);
      } else if (request.getSatisfactions() != null && request.getSatisfactions().length > 0) {
        fitnessFromSatisfactions(request, fn, r);
      } else {
        throw new IllegalArgumentException("Provide either problem or satisfactions.");
      }
    } catch (IllegalArgumentException e) {
      r.setError(describe(e));
    } catch (Exception e) {
      r.setError("Evaluation failed: " + describe(e));
    }
    return r;
  }

  private void fitnessFromSatisfactions(SmFitnessCheckRequest request, String fn,
      FunctionCheckResult r) {
    double[] satisfactions = request.getSatisfactions();
    int n = satisfactions.length;
    int[] sets = request.getSetIndices();
    if (sets == null || sets.length == 0) {
      sets = new int[n];
      for (int i = 0; i < n; i++) {
        sets[i] = i < (n + 1) / 2 ? 0 : 1;
      }
      r.getNotes().add("setIndices not given: split individuals half/half into set 0 and 1.");
    } else if (sets.length != n) {
      throw new IllegalArgumentException("setIndices length must equal satisfactions length.");
    }
    int[] capacities = new int[n];
    Arrays.fill(capacities, 1);
    MatchingData data = new MatchingData(n, 0, sets, capacities, null, null, null);
    TwoSetFitnessEvaluator evaluator = new TwoSetFitnessEvaluator(data);
    r.setSyntaxValid(true);
    try {
      double value = fn.isEmpty()
          ? evaluator.defaultFitnessEvaluation(satisfactions)
          : evaluator.withFitnessFunctionEvaluation(satisfactions, fn);
      r.setValue(value);
      r.setSuccess(true);
    } catch (Exception e) {
      r.setError("Evaluation failed: " + describe(e));
    }
    r.getDetails().put("satisfactions", satisfactions);
  }

  private void fitnessFromDataset(SmFitnessCheckRequest request, String fn,
      FunctionCheckResult r) {
    StableMatchingProblemDto dto = request.getProblem();
    dto.setFitnessFunction(fn);
    String type = request.getMatchingType() == null ? "MTM"
        : request.getMatchingType().trim().toUpperCase();
    MatchingProblem problem = switch (type) {
      case "MTM" -> StableMatchingProblemMapper.toMTM(dto);
      case "OTM" -> StableMatchingProblemMapper.toOTM(dto);
      default -> throw new IllegalArgumentException(
          "Unsupported matchingType '" + type + "', use MTM or OTM.");
    };

    int n = problem.getMatchingData().getSize();
    int[] permutation = request.getPermutation();
    if (permutation == null || permutation.length == 0) {
      permutation = new int[n];
      for (int i = 0; i < n; i++) {
        permutation[i] = i;
      }
      r.getNotes().add("permutation not given: using identity order 0..n-1.");
    }
    if (permutation.length != n || Arrays.stream(permutation).distinct().count() != n
        || Arrays.stream(permutation).anyMatch(v -> v < 0 || v >= n)) {
      throw new IllegalArgumentException(
          "permutation must contain each of 0.." + (n - 1) + " exactly once.");
    }

    Solution solution = problem.newSolution();
    Variable variable = solution.getVariable(0);
    if (!(variable instanceof Permutation)) {
      throw new IllegalArgumentException("Problem does not use a permutation variable.");
    }
    EncodingUtils.setPermutation(variable, permutation);

    problem.evaluate(solution);
    r.setSyntaxValid(true);
    Object matches = solution.getAttribute(StableMatchingConst.MATCHES_KEY);
    if (!(matches instanceof Matches)) {
      r.setError("Matching violates an excluded pair; fitness is not defined for it.");
      return;
    }
    Matches m = (Matches) matches;
    double[] satisfactions = problem.getMatchesSatisfactions(m);
    List<List<Integer>> matchList = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      matchList.add(new ArrayList<>(m.getSetOf(i)));
    }
    r.setValue(-solution.getObjective(0));
    r.setSuccess(true);
    r.getDetails().put("permutation", permutation);
    r.getDetails().put("matches", matchList);
    r.getDetails().put("satisfactions", satisfactions);
    r.getDetails().put("leftOvers", m.getLeftOvers());
  }

  // ---------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------

  /**
   * Builds the matching data from dto the same way the mapper does.
   *
   * @param dto problem dto
   * @return matching data
   */
  public static MatchingData buildMatchingData(StableMatchingProblemDto dto) {
    if (dto.getIndividualSetIndices() == null || dto.getIndividualProperties() == null
        || dto.getIndividualWeights() == null || dto.getIndividualRequirements() == null) {
      throw new IllegalArgumentException(
          "problem needs individualSetIndices, individualProperties, individualWeights "
              + "and individualRequirements.");
    }
    Requirement[][] requirements = RequirementDecoder.decode(dto.getIndividualRequirements());
    int size = dto.getNumberOfIndividuals() > 0
        ? dto.getNumberOfIndividuals() : dto.getIndividualSetIndices().length;
    MatchingData data = new MatchingData(size,
        dto.getNumberOfProperty(),
        dto.getIndividualSetIndices(),
        dto.getIndividualCapacities(),
        dto.getIndividualProperties(),
        dto.getIndividualWeights(),
        requirements);
    data.setExcludedPairs(dto.getExcludedPairs());
    return data;
  }

  private static void checkIndex(int index, int size, String name) {
    if (index < 0 || index >= size) {
      throw new IllegalArgumentException(name + " " + index + " out of range [0, " + size + ").");
    }
  }

  private void applyReference(FunctionCheckResult r, Double value, double reference) {
    r.setReferenceValue(reference);
    boolean differs = value == null
        || Math.abs(value - reference) > TOLERANCE * Math.max(1.0, Math.abs(reference));
    r.setMismatch(differs);
    if (differs) {
      r.getNotes().add(MISMATCH_HINT);
    }
  }

  private double referenceEvaluate(String fn, Set<String> names, Map<String, Double> values) {
    Expression expression = new ExpressionBuilder(fn)
        .function(LOGB)
        .variables(names)
        .build();
    expression.setVariables(values);
    return expression.evaluate();
  }

  private Set<String> findVariables(String fn, Pattern pattern) {
    Set<String> names = new LinkedHashSet<>();
    Matcher matcher = pattern.matcher(fn);
    while (matcher.find()) {
      names.add(matcher.group());
    }
    return names;
  }

  private boolean isDefaultKeyword(String fn) {
    return fn.isBlank() || Arrays.stream(StringExpressionEvaluator.DefaultFunction.values())
        .anyMatch(f -> f.name().equalsIgnoreCase(fn));
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String describe(Throwable e) {
    String message = e.getMessage();
    return e.getClass().getSimpleName() + (message == null ? "" : ": " + message);
  }
}
