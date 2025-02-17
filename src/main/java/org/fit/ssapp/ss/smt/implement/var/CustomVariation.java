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

    Solution[] offsprings = { parents[0].copy(), parents[1].copy() };

    if ((PRNG.nextDouble() <= crossoverRate)) {
      offsprings[0] =  crossover(parents[0], parents[1]);
      offsprings[1] = crossover(parents[1], parents[0]);
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
   * Split the gene of P1, taking the left half.
   * Place the left half of P1 into a new gene, leaving the other half empty.
   * Iterate through the gene of P2 from the beginning,
   * filling the remaining empty half if the values do not already exist in the new gene.
   *
   * @param p1 first parents
   * @param p2 second parents
   */
  public Solution crossover(Solution p1, Solution p2) {
    int crossoverPoint = problemSize / 2;
    Solution offspring = p1.copy();

    Set<Double> existValue = new HashSet<>();

    for (int i = 0; i < crossoverPoint; i++) {
      CustomIntegerVariable v = (CustomIntegerVariable) offspring.getVariable(i);
      existValue.add(v.getValue());
    }

    int parent2Start = crossoverPoint;
    for (int i = 0; i < problemSize && parent2Start < problemSize; i++) {
      CustomIntegerVariable v2 = (CustomIntegerVariable) p2.getVariable(i);
      double value = v2.getValue();

      if (!existValue.contains(value)) {
        CustomIntegerVariable v = (CustomIntegerVariable) offspring.getVariable(parent2Start);
        v.setValue(value);
        existValue.add(value);
        parent2Start++;
      }
    }

    if (parent2Start < problemSize) {
      for (int value = 0; value < problemSize && parent2Start < problemSize; value++) {
        if (!existValue.contains((double) value)) {
          CustomIntegerVariable v = (CustomIntegerVariable) offspring.getVariable(parent2Start);
          v.setValue(value);
          parent2Start++;
        }
      }
    }

    return offspring;

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

}
