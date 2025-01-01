package org.fit.ssapp.ss.smt.preference.impl.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.preference.PreferenceBuilder;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.ss.smt.preference.impl.list.TripletPreferenceList;
import org.fit.ssapp.ss.smt.requirement.Requirement;
import org.fit.ssapp.util.PreferenceProviderUtils;

public class TripletPreferenceProvider implements PreferenceBuilder {

  private final MatchingData individuals;
  private final int numberOfIndividuals;
  @Getter
  private final Map<Integer, Integer> setSizes;

  @Getter
  private final Map<Integer, Expression> expressions;
  private final Map<Integer, Map<String, Set<Integer>>> variables;

  public TripletPreferenceProvider(MatchingData individuals, String[] evaluationFunctions) {
    this.individuals = individuals;
    this.setSizes = new HashMap<>();
    this.expressions = new HashMap<>();
    this.variables = new HashMap<>();
    this.numberOfIndividuals = individuals.getSize();

    // Xác định kích thước từng tập hợp từ dữ liệu
    for (int i = 0; i < numberOfIndividuals; i++) {
      int set = individuals.getSetNoOf(i);
      setSizes.put(set, setSizes.getOrDefault(set, 0) + 1);
    }

    // Xây dựng biểu thức và các biến cho từng tập hợp
    for (int set = 0; set < evaluationFunctions.length; set++) {
      String evalFunction = evaluationFunctions[set];
      Map<String, Set<Integer>> vars = PreferenceProviderUtils.filterVariable(evalFunction);
      Expression expr = new ExpressionBuilder(evalFunction)
          .variables(PreferenceProviderUtils.convertMapToSet(vars))
          .build();
      variables.put(set, vars);
      expressions.put(set, expr);
    }
  }

  // Calculate cumulative padding for a specific set
  private int calculatePaddingForSet(int targetSet) {
    int padding = 0;
    for (int set : setSizes.keySet()) {
      if (set < targetSet) {
        padding += setSizes.get(set);
      }
    }
    return padding;
  }

  @Override
  public PreferenceList getPreferenceListByFunction(int index) {
    int set = individuals.getSetNoOf(index);
    TripletPreferenceList a = new TripletPreferenceList(0, 0);
    Expression e;
    int size = 0;
    if (setSizes.containsKey(set)) {          // 1 2 3 4 5   6 7 8 9 10
      for (int setNumber : setSizes.keySet()) {
        if (setNumber != set) {
          size += setSizes.get(setNumber);
        }
      }
      if (set == 0) {
        a = new TripletPreferenceList(size,
            setSizes.get(0));    // khởi tạo preferlist với size = 2 set còn lại + vào
      } else {
        a = new TripletPreferenceList(size, 0);
      }
      if (this.expressions.get(set) == null) {
        return this.getPreferenceListByDefault(index);
      }
      e = this.expressions.get(set);

      // Xử lý từng set riêng biệt
      int tempIndex = 0;

      for (int otherSet : setSizes.keySet()) {
        if (otherSet != set) {
          int setSize = setSizes.get(otherSet);

          // tạo scores và position tạm thời cho 1 set cụ thể để sort
          // ví dụ hiện tại Node 1 từ set 0 và cần tạo prefer líst đối với set 1 2 thì cần sort từng set trước khi thêm vào preferLíst
          double[] tempScores = new double[setSize];
          int[] tempPositions = new int[setSize];

          int currentIndex = 0;
          for (int i = 0; i < numberOfIndividuals; i++) {
            if (individuals.getSetNoOf(i) == otherSet) {
              e.setVariables(this.getVariableValuesForSet(set, index, i));
              tempScores[currentIndex] = e.evaluate();
              tempPositions[currentIndex] = tempIndex;
              currentIndex++;
              tempIndex++;
            }
          }
          // Sort mảng tạm (sử dụng hàm sort bên ngoài)
          sortDescendingByScores(tempScores, tempPositions);
          // Add vào PreferenceList chính
          a.addArray(tempScores, tempPositions);
        }
      }

    }
    return a;
  }

  private void sortDescendingByScores(double[] tempScores, int[] tempPositions) {
    int size = tempScores.length;

    // Build min heap
    for (int i = size / 2 - 1; i >= 0; i--) {
      heapify(tempScores, tempPositions, size, i);
    }

    // Extract elements from heap one by one
    for (int i = size - 1; i > 0; i--) {
      // Move current root to end
      double tempScore = tempScores[0];
      int tempPos = tempPositions[0];

      tempScores[0] = tempScores[i];
      tempPositions[0] = tempPositions[i];

      tempScores[i] = tempScore;
      tempPositions[i] = tempPos;

      // Call min heapify on the reduced heap
      heapify(tempScores, tempPositions, i, 0);
    }
  }

  private void heapify(double[] tempScores, int[] tempPositions, int heapSize, int rootIndex) {
    int smallestIndex = rootIndex;
    int leftChildIndex = 2 * rootIndex + 1;
    int rightChildIndex = 2 * rootIndex + 2;

    // If left child is smaller than root
    if (leftChildIndex < heapSize && tempScores[leftChildIndex] < tempScores[smallestIndex]) {
      smallestIndex = leftChildIndex;
    }

    // If right child is smaller than smallest so far
    if (rightChildIndex < heapSize && tempScores[rightChildIndex] < tempScores[smallestIndex]) {
      smallestIndex = rightChildIndex;
    }

    // If smallest is not root
    if (smallestIndex != rootIndex) {
      // Swap scores
      double swapScore = tempScores[rootIndex];
      tempScores[rootIndex] = tempScores[smallestIndex];
      tempScores[smallestIndex] = swapScore;

      // Swap positions
      int swapPos = tempPositions[rootIndex];
      tempPositions[rootIndex] = tempPositions[smallestIndex];
      tempPositions[smallestIndex] = swapPos;

      // Recursively heapify the affected sub-tree
      heapify(tempScores, tempPositions, heapSize, smallestIndex);
    }
  }

  public Map<String, Double> getVariableValuesForSet(int set, int indexOfEvaluator,
      int indexOfBeEvaluated) {
    return getVariableValues(this.variables.get(set), indexOfEvaluator, indexOfBeEvaluated);
  }

  private Map<String, Double> getVariableValues(Map<String, Set<Integer>> variables,
      int idx1,
      int idx2) {
    Map<String, Double> variablesValues = new HashMap<>();
    for (Map.Entry<String, Set<Integer>> entry : variables.entrySet()) {
      String key = entry.getKey();
      Set<Integer> values = entry.getValue();
      switch (key) {
        case "P":
          for (Integer value : values) {
            double val = individuals.getPropertyValueOf(idx2, value - 1);
            variablesValues.put(key + value, val);
          }
          break;
        case "W":
          for (Integer value : values) {
            double val = individuals.getPropertyWeightOf(idx1, value - 1);
            variablesValues.put(key + value, val);
          }
          break;
        case "R":
          for (Integer value : values) {
            double val = individuals
                .getRequirementOf(idx1, value - 1)
                .getValueForFunction();
            variablesValues.put(key + value, val);
          }
          break;
        default:
          double val = 0d;
          variablesValues.put(key, val);
      }
    }
    return variablesValues;
  }

  @Override
  public PreferenceList getPreferenceListByDefault(int index) {
    int set = individuals.getSetNoOf(index);
    int numberOfProperties = individuals.getPropertyNum();
    TripletPreferenceList a = new TripletPreferenceList(0, 0);
    int size = 0;

    if (setSizes.containsKey(set)) {
      for (int setNumber : setSizes.keySet()) {
        if (setNumber != set) {
          size += setSizes.get(set);
        }
      }
      if (set == 0) {
        a = new TripletPreferenceList(size,
            setSizes.get(0));    // khởi tạo preferlist với size = 2 set còn lại + vào
      } else {
        a = new TripletPreferenceList(size, 0);
      }

      int tempIndex = 0;
      for (int otherSet : setSizes.keySet()) {
        if (otherSet != set) {
          int setSize = setSizes.get(otherSet);
          double[] tempScores = new double[setSize];
          int[] tempPositions = new int[setSize];

          int currentIndex = 0;
          for (int i = 0; i < numberOfIndividuals; i++) {
            if (individuals.getSetNoOf(i) == otherSet) {
              double totalScore = 0;
              for (int j = 0; j < numberOfProperties; j++) {
                double PropertyValue = individuals.getPropertyValueOf(i, j);
                Requirement requirement = individuals.getRequirementOf(index, j);
                double PropertyWeight = individuals.getPropertyWeightOf(index, j);
                totalScore += requirement.getDefaultScaling(PropertyValue) * PropertyWeight;
              }
              tempScores[currentIndex] = totalScore;
              tempPositions[currentIndex] = tempIndex;
              tempIndex++;
              currentIndex++;
            }
          }

          sortDescendingByScores(tempScores, tempPositions);

          a.addArray(tempScores, tempPositions);
        }
      }

    }
    return a;
  }


  @Override
  public PreferenceListWrapper toListWrapper() {
    List<PreferenceList> lists = new ArrayList<>();
    for (int i = 0; i < individuals.getSize(); i++) {
      lists.add(this.getPreferenceListByFunction(i));
    }
    return new PreferenceListWrapper(lists);
  }
}
