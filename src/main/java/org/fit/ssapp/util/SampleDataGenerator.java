package org.fit.ssapp.util;

import static org.fit.ssapp.constants.StableMatchingConst.DEFAULT_EVALUATE_FUNC;
import static org.fit.ssapp.constants.StableMatchingConst.DEFAULT_FITNESS_FUNC;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.StableMatchingConst.ReqTypes;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 * Stable Matching Problem Testing Space.
 */
@Data
@Slf4j
@AllArgsConstructor
public class SampleDataGenerator {

  private static final Random RANDOM = new Random();
  public Map<Integer, Integer> setCapacities = new HashMap<>();
  boolean[] capRandomize = {true, true};
  // Configuration parameters
  private MatchingProblemType matchingProblemType;
  // problemSize
  private int individualNum;
  private int numberOfProperties;
  private int[] numberForeachSet;
  private String[] evaluateFunctions = {DEFAULT_EVALUATE_FUNC, DEFAULT_EVALUATE_FUNC};
  private String fnf = DEFAULT_FITNESS_FUNC;

  /**
   * Constructs a SampleDataGenerator with the specified matching problem type and parameters.
   *
   * @param matchingProblemType The type of matching problem (MTM, OTM, OTO).
   * @param numberOfSet1        Number of individuals in the first set.
   * @param numberOfSet2        Number of individuals in the second set.
   * @param numberOfProperties  Number of properties per individual.
   */
  public SampleDataGenerator(MatchingProblemType matchingProblemType, int numberOfSet1,
      int numberOfSet2, int numberOfProperties) {
    if (numberOfSet1 <= 0 || numberOfSet2 <= 0 || numberOfProperties <= 0) {
      throw new IllegalArgumentException("Number of sets and properties must be greater than 0");
    }
    if (matchingProblemType == null) {
      throw new IllegalArgumentException("Matching Problem Type should be MTM, OTM or OTO");
    }
    this.matchingProblemType = matchingProblemType;
    this.numberForeachSet = new int[2];
    this.individualNum = numberOfSet1 + numberOfSet2;
    this.numberForeachSet[0] = numberOfSet1;
    this.numberForeachSet[1] = numberOfSet2;
    this.setCapacities.put(0, 10);
    this.setCapacities.put(1, 10);
    this.numberOfProperties = numberOfProperties;

    switch (this.matchingProblemType) {
      case MTM -> this.capRandomize = new boolean[]{true, true};
      case OTM -> this.capRandomize = new boolean[]{true, false};
      case OTO -> this.capRandomize = new boolean[]{false, false};
      default -> log.warn("Unknown Matching Problem Type");
    }
  }

  /**
   * Generates a StableMatchingProblemDto instance based on the configured parameters.
   */
  public SampleDataGenerator(MatchingProblemType matchingProblemType, int[] numberForeachSet,
      int numberOfProperties) {
    this.matchingProblemType = matchingProblemType;
    this.numberForeachSet = numberForeachSet;
    this.numberOfProperties = numberOfProperties;

    if (matchingProblemType == null) {
      throw new IllegalArgumentException("Matching Problem Type should be MTM, OTM or OTO");
    }

    switch (this.matchingProblemType) {
      case MTM -> this.capRandomize = new boolean[]{true, true};
      case OTM -> this.capRandomize = new boolean[]{true, false};
      case OTO -> this.capRandomize = new boolean[]{false, false};
      default -> throw new IllegalArgumentException(
          "Unknown Matching Problem Type:" + this.matchingProblemType);
    }
  }

  /**
   * Main method to demonstrate usage.
   */
  public static void main(String[] args) {
    int numberOfProperties = 5;
    SampleDataGenerator generator = new SampleDataGenerator(MatchingProblemType.MTM, 20, 200,
        numberOfProperties);
    generator.setCapacities.put(0, 5);
    generator.setCapacities.put(1, 5);
    generator.setCapRandomize(new boolean[]{true, true});
    generator.setEvaluateFunctions(new String[]{DEFAULT_EVALUATE_FUNC, DEFAULT_EVALUATE_FUNC});
    generator.setFnf(DEFAULT_FITNESS_FUNC);

    String algo = "IBEA";
    MatchingProblem problem = generator.generateProblem();
    // Run the algorithm
    long startTime = System.currentTimeMillis();
    NondominatedPopulation result = new Executor()
        .withProblem(problem)
        .withAlgorithm(algo)
        .withMaxEvaluations(100)
        .withProperty("populationSize", 1000)
        .distributeOnAllCores()
        .run();

    long endTime = System.currentTimeMillis();
    double runtime = ((double) (endTime - startTime) / 1000);
    runtime = Math.round(runtime * 100.0) / 100.0;

    // Process and print the results
    for (Solution solution : result) {
      System.out.println("Matches: " + solution.getAttribute("matches"));
      System.out.println("Fitness Score: " + -solution.getObjective(0));
    }
    System.out.println("\nExecution time: " + runtime + " Second(s) with Algorithm: " + algo);

  }

  /**
   * Generates a StableMatchingProblemDto instance based on the configured parameters.
   *
   * @return A StableMatchingProblemDto object
   */
  public StableMatchingProblemDto generateDto() {
    StableMatchingProblemDto problemDto = new StableMatchingProblemDto();
    problemDto.setNumberOfIndividuals(individualNum);
    problemDto.setNumberOfSets(numberForeachSet.length);
    problemDto.setNumberOfProperty(numberOfProperties);
    problemDto.setIndividualSetIndices(generateSetIndices());
    problemDto.setIndividualCapacities(generateCapacities());
    problemDto.setIndividualProperties((double[][]) generatePw().get("property"));
    problemDto.setIndividualWeights((double[][]) generatePw().get("weight"));
    problemDto.setIndividualRequirements(generateRequirementString());
    problemDto.setEvaluateFunctions(evaluateFunctions);
    problemDto.setFitnessFunction(fnf);
    return problemDto;
  }

  /**
   * Generates a StableMatchingRBOProblem instance based on the configured parameters.
   *
   * @return StableMatchingRBOProblem
   */
  public MatchingProblem generateProblem() {
    MatchingProblem matchingProblem;

    StableMatchingProblemDto newDto = this.generateDto();
    switch (this.matchingProblemType) {
      case MTM -> matchingProblem = StableMatchingProblemMapper.toMTM(newDto);
      case OTM -> matchingProblem = StableMatchingProblemMapper.toOTM(newDto);
      case OTO -> matchingProblem = StableMatchingProblemMapper.toOTO(newDto);
      default -> {
        log.info("[ERROR] Match Problem Type hasn't been initialized yet. Terminated...");
        matchingProblem = null;
      }
    }

    return matchingProblem;
  }

  /**
   * Adds properties to an individual.
   */
  private Map<String, Object> generatePw() {
    Map<String, Object> result = new HashMap<>();
    double[][] individualProperties = new double[this.individualNum][this.numberOfProperties];
    double[][] individualWeights = new double[this.individualNum][this.numberOfProperties];
    for (int i = 0; i < this.individualNum; i++) {
      for (int j = 0; j < numberOfProperties; j++) {
        // Example property values
        double propertyValue = RANDOM.nextDouble() * (70.0 - 20.0) + 20.0;
        double propertyWeight = 1 + (10 - 1) * RANDOM.nextDouble();
        individualProperties[i][j] = propertyValue;
        individualWeights[i][j] = propertyWeight;
      }
    }

    result.put(ObjectKeys.PROPERTY, individualProperties);
    result.put(ObjectKeys.WEIGHT, individualWeights);
    return result;
  }

  private String[][] generateRequirementString() {
    String[][] individualRequirements = new String[this.individualNum][this.numberOfProperties];

    String[] expression = {"", "--", "++"};
    for (int i = 0; i < this.individualNum; i++) {
      for (int j = 0; j < numberOfProperties; j++) {
        String requirement;
        int randomType = RANDOM.nextInt(2) + 1;
        double propertyBound = RANDOM.nextDouble() * (70.0 - 20.0) + 20.0;

        if (ReqTypes.ONE_BOUND == randomType) {
          int randomExpression = RANDOM.nextInt(2) + 1;
          requirement = propertyBound + expression[randomExpression];
        } else {
          double propertyBound2 = RANDOM.nextDouble() * (70.0 - 20.0) + 20.0;
          requirement = propertyBound + ":" + propertyBound2;
          //  if (ReqTypes.SCALE_TARGET == randomType)
        }
        individualRequirements[i][j] = requirement;
      }

    }
    return individualRequirements;
  }


  private int[] generateSetIndices() {
    int[] setIndices = new int[individualNum];
    int currentIndex = 0;
    for (int i = 0; i < numberForeachSet.length; i++) {
      int setSize = numberForeachSet[i];
      for (int j = 0; j < setSize; j++) {
        setIndices[currentIndex++] = i;
      }
    }
    return setIndices;
  }

  private int[] generateCapacities() {
    int[] capacities = new int[individualNum];
    int currentIndex = 0;
    for (int i = 0; i < numberForeachSet.length; i++) {
      int setSize = numberForeachSet[i];
      int setCapacity = setCapacities.get(i);
      for (int j = 0; j < setSize; j++) {
        if (capRandomize[i]) {
          capacities[currentIndex++] =
              1 + RANDOM.nextInt(setCapacity - 1); // Random capacity between 1 and setCapacity
        } else {
          capacities[currentIndex++] = setCapacity;
        }
      }
    }
    return capacities;
  }

  private interface ObjectKeys {

    String PROPERTY = "property";
    String WEIGHT = "weight";
  }

}
