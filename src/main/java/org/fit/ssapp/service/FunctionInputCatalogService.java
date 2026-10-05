package org.fit.ssapp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.fit.ssapp.util.StringExpressionEvaluator.DefaultFunction;
import org.springframework.stereotype.Service;

/**
 * Lists the input points ("đầu điểm") a user can use when filling a function, based on the data
 * of the uploaded / typed problem. Pure data description, nothing is evaluated here.
 */
@Service
public class FunctionInputCatalogService {

  /**
   * Describe inputs of a game theory problem.
   *
   * @param players players (may be empty / null for a blank manual start)
   * @return catalog
   */
  public Map<String, Object> describeGameTheory(List<NormalPlayer> players) {
    int playerCount = players == null ? 0 : players.size();
    int propertyCount = 0;
    List<Map<String, Object>> playerItems = new ArrayList<>();
    for (int i = 0; i < playerCount; i++) {
      NormalPlayer player = players.get(i);
      List<Map<String, Object>> strategyItems = new ArrayList<>();
      List<Strategy> strategies = player.getStrategies() == null
          ? List.of() : player.getStrategies();
      for (int s = 0; s < strategies.size(); s++) {
        Strategy strategy = strategies.get(s);
        List<Double> props = strategy.getProperties();
        propertyCount = Math.max(propertyCount, props == null ? 0 : props.size());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("index", s);
        item.put("name", strategy.getName());
        item.put("properties", props);
        strategyItems.add(item);
      }
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("index", i);
      item.put("label", "Player " + (i + 1));
      item.put("name", player.getName());
      item.put("payoffFunction", player.getPayoffFunction());
      item.put("strategies", strategyItems);
      playerItems.add(item);
    }

    List<Map<String, String>> payoffVariables = new ArrayList<>();
    for (int k = 1; k <= propertyCount; k++) {
      payoffVariables.add(variable("p" + k,
          "Property " + k + " of the strategy the player is evaluating"));
    }
    if (playerCount > 1 && propertyCount > 0) {
      payoffVariables.add(variable("P{j}p{i}",
          "Property i of the strategy chosen by player j (relative payoff), e.g. P2p1"));
    }
    List<Map<String, String>> fitnessVariables = new ArrayList<>();
    for (int k = 1; k <= playerCount; k++) {
      fitnessVariables.add(variable("u" + k, "Payoff of player " + k));
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("problemType", "GT");
    result.put("playerCount", playerCount);
    result.put("propertyCount", propertyCount);
    result.put("players", playerItems);
    result.put("payoffVariables", payoffVariables);
    result.put("fitnessVariables", fitnessVariables);
    result.put("defaultFunctions", Arrays.stream(DefaultFunction.values())
        .map(Enum::name).toList());
    return result;
  }

  /**
   * Describe inputs of a stable matching problem.
   *
   * @param dto problem dto (arrays may be partially empty)
   * @return catalog
   */
  public Map<String, Object> describeStableMatching(StableMatchingProblemDto dto) {
    int[] sets = dto.getIndividualSetIndices() == null ? new int[0] : dto.getIndividualSetIndices();
    int size = sets.length;
    int propertyCount = dto.getNumberOfProperty();
    List<Map<String, Object>> individuals = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("index", i);
      item.put("label", "Individual " + (i + 1));
      item.put("set", sets[i]);
      item.put("capacity", at(dto.getIndividualCapacities(), i));
      item.put("properties", at(dto.getIndividualProperties(), i));
      item.put("weights", at(dto.getIndividualWeights(), i));
      item.put("requirements", at(dto.getIndividualRequirements(), i));
      individuals.add(item);
    }

    List<Map<String, String>> evaluateVariables = new ArrayList<>();
    for (int k = 1; k <= propertyCount; k++) {
      evaluateVariables.add(variable("P" + k, "Property " + k + " of the individual being evaluated"));
      evaluateVariables.add(variable("W" + k, "Weight of property " + k + " of the evaluator"));
      evaluateVariables.add(variable("R" + k, "Requirement " + k + " of the evaluator (numeric value)"));
    }
    List<Map<String, String>> fitnessVariables = new ArrayList<>();
    for (int k = 1; k <= size; k++) {
      fitnessVariables.add(variable("M" + k, "Satisfaction of individual " + k + " in the matching"));
    }
    fitnessVariables.add(variable("S(k)", "Sum of satisfactions of set k (see TwoSetFitnessEvaluator)"));
    fitnessVariables.add(variable("SIGMA{expr}", "Sum of expr over the individuals of a set, expr uses S1 or S2"));

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("problemType", "SM");
    result.put("individualCount", size);
    result.put("propertyCount", propertyCount);
    result.put("individuals", individuals);
    result.put("evaluateVariables", evaluateVariables);
    result.put("fitnessVariables", fitnessVariables);
    result.put("needsMatching", "Fitness is computed on a matching produced from a permutation, "
        + "not on raw data: send a permutation or use the default order.");
    return result;
  }

  private static Map<String, String> variable(String name, String description) {
    Map<String, String> v = new LinkedHashMap<>();
    v.put("name", name);
    v.put("description", description);
    return v;
  }

  private static Object at(Object[] array, int i) {
    return array != null && i < array.length ? array[i] : null;
  }

  private static Object at(int[] array, int i) {
    return array != null && i < array.length ? array[i] : null;
  }

  private static Object at(double[][] array, int i) {
    return array != null && i < array.length ? array[i] : null;
  }
}
