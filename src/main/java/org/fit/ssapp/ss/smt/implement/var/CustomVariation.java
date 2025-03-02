//package org.fit.ssapp.ss.smt.implement.var;
//
//import lombok.extern.slf4j.Slf4j;
//import org.moeaframework.core.PRNG;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.Variation;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * CustomVariation, this class will implement crossover - mutation - evolve phase of the GA system.
// */
//@Slf4j
//public class CustomVariation implements Variation {
//
//  /**
//   * the probability that crossover happens in the system.
//   */
//  private final double crossoverRate;
//  /**
//   * the probability that crossover happens in the system.
//   */
//  private final double mutationRate;
//
//  /**
//   * problem size (number of individuals in matching problem).
//   */
//  private final int problemSize;
//
//  /**
//   * problem size (number of individuals in matching problem).
//   */
//  public CustomVariation(double crossoverRate, double mutationRate, int problemSize) {
//    this.crossoverRate = crossoverRate;
//    this.mutationRate = mutationRate;
//    this.problemSize = problemSize;
//  }
//
//  @Override
//  public String getName() {
//    return "Custom_Variation";
//  }
//
//  @Override
//  public int getArity() {
//    return 2;
//  }
//
//  @Override
//  public Solution[] evolve(Solution[] parents) {
//    if (parents.length != getArity()) {
//      throw new IllegalArgumentException("CustomVariation requires exactly 2 parents!");
//    }
//
//    Solution[] offsprings = { parents[0].copy(), parents[1].copy() };
//
//    if ((PRNG.nextDouble() <= crossoverRate)) {
//      offsprings[0] =  crossover(parents[0], parents[1]);
//      offsprings[1] = crossover(parents[1], parents[0]);
//    }
//
//    for (Solution offspring : offsprings) {
//      if ((PRNG.nextDouble() <= mutationRate)) {
//        mutation(offspring);
//      }
//    }
//    return offsprings;
//  }
//
//  /**
//   * Implement crossover phase of the system.
//   * Split the gene of P1, taking the left half.
//   * Place the left half of P1 into a new gene, leaving the other half empty.
//   * Iterate through the gene of P2 from the beginning,
//   * filling the remaining empty half if the values do not already exist in the new gene.
//   *
//   * @param p1 first parents
//   * @param p2 second parents
//   */
//  public Solution crossover(Solution p1, Solution p2) {
//    int crossoverPoint = p2.getNumberOfVariables() / 2;
//    Solution offspring = p1.copy();
//
//    // Set capacity to reduce insert time
//    Set<CustomIntegerVariable> existValue = new HashSet<>(p1.getNumberOfVariables());
//
//    for (int i = 0; i < crossoverPoint; i++) {
//      CustomIntegerVariable v = (CustomIntegerVariable) offspring.getVariable(i);
//      existValue.add(v);
//    }
//
//    {
//      int i = 0;
//      int offspringIdx = crossoverPoint + 1;
//      while (i < p1.getNumberOfVariables() && offspringIdx < p1.getNumberOfVariables())
//      {
//        CustomIntegerVariable v2 = (CustomIntegerVariable) p2.getVariable(i);
//
//        if (!existValue.contains(v2)) {
//          existValue.add(v2);
//          offspring.setVariable(offspringIdx, v2);
//          offspringIdx++;
//        }
//
//        i++;
//      }
//    }
//
//    return offspring;
//  }
//
//  /**
//   * Implement mutation phase of the system.
//   *
//   * @param offspring offspring to be mutated
//   */
//  public void mutation(Solution offspring) {
//    int swapPoint1 = PRNG.nextInt(problemSize);
//    int swapPoint2 = PRNG.nextInt(problemSize);
//
//    while (swapPoint1 == swapPoint2) {
//      swapPoint2 = PRNG.nextInt(problemSize);
//    }
//
//    CustomIntegerVariable v1 = (CustomIntegerVariable) offspring.getVariable(swapPoint1);
//    CustomIntegerVariable v2 = (CustomIntegerVariable) offspring.getVariable(swapPoint2);
//
//    double temp = v1.getValue();
//    v1.setValue(v2.getValue());
//    v2.setValue(temp);
//  }
//}
