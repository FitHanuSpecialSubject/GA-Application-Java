package org.fit.ssapp.ss.smt.implement.var;

import lombok.extern.slf4j.Slf4j;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.HashSet;
import java.util.Set;

/**
 * CustomVariation, this class will implement crossover - mutation - evolve phase of the GA system.
 */
@Slf4j
public class CustomVariation implements Variation {

  /**
   * the probability that crossover happens in the system.
   */
  private final double crossoverRate;
  /**
   * the probability that crossover happens in the system.
   */
  private final double mutationRate;

  /**
   * problem size (number of individuals in matching problem).
   */
  private final int problemSize;

  /**
   * problem size (number of individuals in matching problem).
   */
  public CustomVariation(double crossoverRate, double mutationRate, int problemSize) {
    this.crossoverRate = crossoverRate;
    this.mutationRate = mutationRate;
    this.problemSize = problemSize;
  }

  @Override
  public String getName() {
    return "Custom_Variation";
  }

  @Override
  public int getArity() {
    return 2;
  }

  @Override
  public Solution[] evolve(Solution[] parents) {
    if (parents.length != getArity()) {
      throw new IllegalArgumentException("CustomVariation requires exactly 2 parents!");
    }

    Solution[] offsprings = {parents[0].copy(), parents[1].copy()};

    if ((PRNG.nextDouble() <= crossoverRate)) {
      crossover(offsprings[0], offsprings[1]);
    }

    for (Solution offspring : offsprings) {
      if ((PRNG.nextDouble() <= mutationRate)) {
        mutation(offspring);
      }
    }
    return offsprings;
  }

  /**
   * Implement crossover phase of the system.
   *
   * @param p1 first parents
   * @param p2 second parents
   */
  public void crossover(Solution p1, Solution p2) {
    int crossoverPoint1 = PRNG.nextInt(problemSize - 1);
    int crossoverPoint2 = PRNG.nextInt(problemSize - 1);

    if (crossoverPoint1 > crossoverPoint2) {
      int temp = crossoverPoint1;
      crossoverPoint1 = crossoverPoint2;
      crossoverPoint2 = temp;
    }

    for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
      CustomIntegerVariable v1 = (CustomIntegerVariable) p1.getVariable(i);
      CustomIntegerVariable v2 = (CustomIntegerVariable) p2.getVariable(i);

      double temp = v1.getValue();
      v1.setValue(v2.getValue());
      v2.setValue(temp);
    }

    repair(p1);
    repair(p2);

  }

  /**
   * Implement mutation phase of the system.
   *
   * @param offspring offspring to be mutated
   */
  public void mutation(Solution offspring) {
    int swapPoint1 = PRNG.nextInt(problemSize);
    int swapPoint2 = PRNG.nextInt(problemSize);

    while (swapPoint1 == swapPoint2) {
      swapPoint2 = PRNG.nextInt(problemSize);
    }

    CustomIntegerVariable v1 = (CustomIntegerVariable) offspring.getVariable(swapPoint1);
    CustomIntegerVariable v2 = (CustomIntegerVariable) offspring.getVariable(swapPoint2);
    //    log.info("Before Swap: index1 = {}, value1 = {}, index2 = {}, value2 = {}",
    //            swapPoint1, v1.getValue(), swapPoint2, v2.getValue());

    double temp = v1.getValue();
    v1.setValue(v2.getValue());
    v2.setValue(temp);

    //    log.info("AFter Swap: index1 = {}, value1 = {}, index2 = {}, value2 = {}",
    //            swapPoint1, v1.getValue(), swapPoint2, v2.getValue());

  }

  /**
   * repair when repeat value appears after crossover.
   *
   * @param offspring offspring to be repaired
   */
  public void repair(Solution offspring) {
    Set<Double> fixRepeat = new HashSet<>();

    for (int i = 0; i < problemSize; i++) {
      CustomIntegerVariable v = (CustomIntegerVariable) offspring.getVariable(i);
      double value = v.getValue();

      while (fixRepeat.contains(value)) {
        value = PRNG.nextInt(problemSize);
      }

      v.setValue(value);
      fixRepeat.add(value);
    }
  }


}
