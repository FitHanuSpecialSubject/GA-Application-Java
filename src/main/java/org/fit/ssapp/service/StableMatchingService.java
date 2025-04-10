package org.fit.ssapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Progress;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.exception.AlgorithmsUniformException;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.ss.smt.preference.PreferenceBuilder;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.ss.smt.preference.impl.list.TripletPreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.fit.ssapp.ss.smt.result.MatchingSolution;
import org.fit.ssapp.ss.smt.result.MatchingSolutionInsights;
import org.fit.ssapp.util.ComputerSpecsUtil;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;
import org.moeaframework.core.termination.MaxFunctionEvaluations;
import org.moeaframework.util.TypedProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * StableMatchingService - Provides stable matching problem-solving services.
 * This service handles the execution of stable matching algorithms using different approaches.
 * It integrates with **MOEA Framework** for multi-objective optimization and allows:
 * - Solving a stable matching problem using various algorithms.
 * - Benchmarking multiple algorithms to compare performance.
 * - Real-time progress tracking and updates via WebSockets.
 * ## Main Features
 * - Solves stable matching problems based on provided configurations.
 * - Supports parallel execution using multiple computing cores.
 * - Collects performance insights for different algorithms.
 * - Handles WebSocket communication to send progress updates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StableMatchingService implements ProblemService {

  // static {
  //   OperatorFactory.getInstance().addProvider(new OperatorProvider() {
  //     public String getMutationHint(Problem problem) {
  //       return "CustomVariation";
  //     }

  //     public String getVariationHint(Problem problem) {
  //       return "CustomVariation";
  //     }

  //     public Variation getVariation(String name, TypedProperties properties, Problem problem) {
  //       if (name.equalsIgnoreCase("CustomVariation")) {
  //         double crossoverRate = properties.getDouble("CustomVariation.crossoverRate", 0.9);
  //         double mutationRate = properties.getDouble("CustomVariation.mutationRate", 0.1);
  //         return new CustomVariation(crossoverRate, mutationRate, problem.getNumberOfVariables());
  //       }
  //       return null;
  //     }
  //   });
  // }

  private static final int RUN_COUNT_PER_ALGORITHM = 10;
  private final SimpMessagingTemplate simpMessagingTemplate;

  /**
   * Solves a stable matching problem based on the given request.
   *
   * @param request request The stable matching problem configuration.
   */
  public ResponseEntity<Response> solve(StableMatchingProblemDto request) {

    try {
      log.info("Validating StableMatchingProblemDto Request ...");

      MatchingProblem problem = StableMatchingProblemMapper.toMTM(request);
      //            Unsafe unsafe;
      //            try {
      //                Field field = Unsafe.class.getDeclaredField("theUnsafe");
      //                field.setAccessible(true);
      //                unsafe = (Unsafe) field.get(null);
      //            } catch (Exception e) {
      //                throw new AssertionError(e);
      //            }
      //            long requestAddress = getAddress(unsafe, request.getIndividualCapacities());
      //            long problemAddress = getAddress(unsafe, problem.getMatchingData().getCapacities());
      //            log.info("req cap {}, pro cap {}, equality {}", requestAddress, problemAddress,
      //            Objects.equals(requestAddress, problemAddress));
      log.info("Start solving: {}, problem name: {}, problem size: {}",
              problem.getMatchingTypeName(),
              problem.getName(),
              problem.getMatchingData().getSize());
      long startTime = System.currentTimeMillis();

      NondominatedPopulation results = solveProblem(problem,
              request.getAlgorithm(),
              request.getPopulationSize(),
              request.getGeneration(),
              request.getMaxTime(),
              request.getDistributedCores(), request);

      if (Objects.isNull(results)) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response
                        .builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Error solving stable matching problem.")
                        .data(null)
                        .build());
      }
      //            Testing tester = new Testing((Matches) results.get(0).getAttribute("matches"),
      //            problem.getMatchingData().getSize(), problem.getMatchingData().getCapacities());
      //            System.out.println("[Testing] Solution has duplicate: " + tester.hasDuplicate())
      long endTime = System.currentTimeMillis();

      double runtime = ((double) (endTime - startTime) / 1000);
      runtime = (runtime * 1000.0);
      log.info("Runtime: {} Millisecond(s).", runtime);
      //problem.printIndividuals();
      //System.out.println(problem.printPreferenceLists());
      String algorithm = request.getAlgorithm();

      MatchingSolution matchingSolution = formatSolution(algorithm, results, runtime);
      matchingSolution.setSetSatisfactions(problem.getMatchesSatisfactions((Matches) results
              .get(0)
              .getAttribute(StableMatchingConst.MATCHES_KEY)));

      return ResponseEntity.ok(Response
              .builder()
              .status(200)
              .message(
                      "[Service] Stable Matching: Solve stable matching problem successfully!")
              .data(matchingSolution)
              .build());
    } catch (AlgorithmsUniformException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
              Response.builder()
                      .data(null)
                      .message("[Service] Stable Matching: BAD REQUEST")
                      .status(HttpStatus.BAD_REQUEST.value()).build()
      );
    } catch (Exception e) {
      log.error("[Service] Stable Matching: Error solving stable matching problem: {}",
              e.getMessage(),
              e);
      // Handle exceptions and return an error response
      return ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Response
                      .builder()
                      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                      .message(
                              "[Service] Stable Matching: Error solving stable matching problem.")
                      .data(null)
                      .build());
    }
  }

  private MatchingSolution formatSolution(String algorithm,
                                          NondominatedPopulation result,
                                          double Runtime) {
    Solution solution = result.get(0);
    MatchingSolution matchingSolution = new MatchingSolution();
    double fitnessValue = solution.getObjective(0);
    Matches matches = (Matches) solution.getAttribute("matches");

    matchingSolution.setFitnessValue(-fitnessValue);
    matchingSolution.setMatches(matches);
    matchingSolution.setAlgorithm(algorithm);
    matchingSolution.setRuntime(Runtime);
    matchingSolution.setComputerSpecs(ComputerSpecsUtil.getComputerSpecs());

    return matchingSolution;
  }

  //    private static long getAddress(Unsafe unsafe, Object obj) {
  //        Object[] array = new Object[]{obj};
  //        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
  //        return unsafe.getLong(array, baseOffset);
  //    }

  /**
   * **Executes the matching problem using the specified algorithm.**
   *
   * @param problem The stable matching problem instance.
   * @param algorithm The algorithm to use for solving.
   * @param populationSize The population size for evolutionary algorithms.
   * @param generation The number of generations to run.
   * @param maxTime The maximum execution time allowed.
   * @param distributedCores The number of computing cores used for execution.
   * @return A `NondominatedPopulation` containing the solutions.
   */
  private NondominatedPopulation solveProblem(MatchingProblem problem,
                                              String algorithm,
                                              int populationSize,
                                              int generation,
                                              int maxTime,
                                              String distributedCores,
                                              StableMatchingProblemDto request) {
    NondominatedPopulation result;
    if (algorithm == null) {
      algorithm = "PESA2";
    }

    if (distributedCores == null) {
      distributedCores = "all";
    }
    TypedProperties properties = new TypedProperties();
    properties.setInt("populationSize", populationSize);
    properties.setInt("maxTime", maxTime);
    TerminationCondition maxEval = new MaxFunctionEvaluations(generation * populationSize);

    validateUniformPreferences(problem.getMatchingData(), algorithm, request);

    // check for IBEA or eMOEA, if true do test run with minimal configuration

    if (algorithm.equals("IBEA") || algorithm.equals("eMOEA")) {
      try {
        new Executor()
                .withProblem(problem)
                .withAlgorithm(algorithm)
                .withMaxEvaluations(100)
                .withTerminationCondition(new MaxFunctionEvaluations(100))
                .withProperties(properties)
                .distributeOn(1)
                .run();
      } catch (Exception e) {
        throw new AlgorithmsUniformException("uniform preferences found");
      }
    }

    try {
      if (distributedCores.equals("all")) {
        result = new Executor()
                .withProblem(problem)
                .withAlgorithm(algorithm)
                .withMaxEvaluations(generation * populationSize)
                .withTerminationCondition(maxEval)
                .withProperties(properties)
                .distributeOnAllCores()
                .run();
      } else {
        int numberOfCores = Integer.parseInt(distributedCores);
        result = new Executor()
                .withProblem(problem)
                .withAlgorithm(algorithm)
                .withMaxEvaluations(generation * populationSize)
                .withTerminationCondition(maxEval)
                .withProperties(properties)
                .distributeOn(numberOfCores)
                .run();
      }
      log.info("Problem {} solved successfully!", problem.getName());
      return result;
    } catch (Exception e) {
      log.error("Error solving {}, {}", problem.getName(), e.getMessage(), e);
      return null;
    }
  }

  /**
   * getInsights.
   *
   * @param request StableMatchingProblemDto
   * @param sessionCode string
   */
  public ResponseEntity<Response> getInsights(StableMatchingProblemDto request,
                                              String sessionCode) {
    String[] algorithms = StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
            "/progress",
            createProgressMessage("Initializing the problem..."));
    MTMProblem problem = StableMatchingProblemMapper.toMTM(request);

    log.info("Start benchmarking {} session code {}", problem.getName(), sessionCode);

    MatchingSolutionInsights matchingSolutionInsights = initMatchingSolutionInsights(algorithms);

    int runCount = 1;
    int maxRunCount = algorithms.length * RUN_COUNT_PER_ALGORITHM;
    // solve the problem with different algorithms and then evaluate the performance
    // of the algorithms
    //        log.info("Start benchmarking the algorithms...");
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
            "/progress",
            createProgressMessage("Start benchmarking the algorithms..."));

    for (String algorithm : algorithms) {
      for (int i = 0; i < RUN_COUNT_PER_ALGORITHM; i++) {
        System.out.println("Iteration: " + i);
        long start = System.currentTimeMillis();

        NondominatedPopulation results = solveProblem(problem,
                algorithm,
                request.getGeneration(),
                request.getPopulationSize(),
                request.getMaxTime(),
                request.getDistributedCores(), request);

        long end = System.currentTimeMillis();
        assert results != null;
        double runtime = (double) (end - start) / 1000;
        double fitnessValue = getFitnessValue(results);

        // send the progress to the client
        String message =
                "Algorithm " + algorithm + " finished iteration: #" + (i + 1) + "/"
                        + RUN_COUNT_PER_ALGORITHM;
        Progress progress = createProgress(message, runtime, runCount, maxRunCount);
        System.out.println(progress);
        simpMessagingTemplate.convertAndSendToUser(sessionCode, "/progress", progress);
        runCount++;

        // add the fitness value and runtime to the insights
        matchingSolutionInsights.getFitnessValues().get(algorithm).add(-fitnessValue);
        matchingSolutionInsights.getRuntimes().get(algorithm).add(runtime);
      }

    }
    log.info("Benchmark finished! {} session code {}", problem.getName(), sessionCode);
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
            "/progress",
            createProgressMessage("Benchmarking finished!"));

    return ResponseEntity.ok(Response
            .builder()
            .status(200)
            .message("Get problem result insights successfully!")
            .data(matchingSolutionInsights)
            .build());
  }

  private MatchingSolutionInsights initMatchingSolutionInsights(String[] algorithms) {
    MatchingSolutionInsights matchingSolutionInsights = new MatchingSolutionInsights();
    Map<String, List<Double>> fitnessValueMap = new HashMap<>();
    Map<String, List<Double>> runtimeMap = new HashMap<>();

    matchingSolutionInsights.setFitnessValues(fitnessValueMap);
    matchingSolutionInsights.setRuntimes(runtimeMap);
    matchingSolutionInsights.setComputerSpecs(ComputerSpecsUtil.getComputerSpecs());

    for (String algorithm : algorithms) {
      fitnessValueMap.put(algorithm, new ArrayList<>());
      runtimeMap.put(algorithm, new ArrayList<>());
    }

    return matchingSolutionInsights;
  }

  private Progress createProgressMessage(String message) {
    return Progress
            .builder()
            .inProgress(
                    false)
            // this object is just to send a message to the client, not to show the progress
            .message(message)
            .build();
  }

  private Progress createProgress(String message,
                                  Double runtime,
                                  Integer runCount,
                                  int maxRunCount) {
    int percent = runCount * 100 / maxRunCount;
    int minuteLeft = (int) Math.ceil(
            ((maxRunCount - runCount) * runtime) / 60); // runtime is in seconds
    return Progress
            .builder()
            .inProgress(true) // this object is just to send to the client to show the progress
            .message(message)
            .runtime(runtime)
            .minuteLeft(minuteLeft)
            .percentage(percent)
            .build();
  }

  private double getFitnessValue(NondominatedPopulation result) {
    Solution solution = result.get(0);
    return solution.getObjective(0);
  }


  public static void validateUniformPreferences(MatchingData data, String algorithm, StableMatchingProblemDto request) {
    if (!Objects.equals(algorithm, "IBEA") || !Objects.equals(algorithm, "eMOEA")) {
      return;
    }

    PreferenceBuilder builder = new TwoSetPreferenceProvider(data, request.getEvaluateFunctions());
    PreferenceListWrapper preferenceLists = builder.toListWrapper();
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);
    List<PreferenceList> lists = preferenceLists.getLists();
    List<Integer> invalidAgents = new ArrayList<>();

    for (int i = 0; i < lists.size(); i++) {
      PreferenceList list = lists.get(i);
      if ((list instanceof TwoSetPreferenceList twoSet && twoSet.isUniformPreference()) ||
              (list instanceof TripletPreferenceList triplet && triplet.isUniformPreference())) {
        invalidAgents.add(i);
      }
    }

    // Step 3: If uniform preferences found, throw error
    if (!invalidAgents.isEmpty()) {
      throw new AlgorithmsUniformException("uniform preferences found");

    }
  }

}
