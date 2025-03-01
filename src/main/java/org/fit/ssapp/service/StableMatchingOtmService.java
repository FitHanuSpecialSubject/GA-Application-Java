package org.fit.ssapp.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Progress;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
//import org.fit.ssapp.ss.smt.implement.var.CustomVariation;
import org.fit.ssapp.ss.smt.result.MatchingSolution;
import org.fit.ssapp.ss.smt.result.MatchingSolutionInsights;
import org.moeaframework.Executor;
import org.moeaframework.core.*;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.core.termination.MaxFunctionEvaluations;
import org.moeaframework.util.TypedProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * StableMatchingOtmService - Provides stable matching problem-solving services.
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
public class StableMatchingOtmService {

  private final SimpMessagingTemplate simpMessagingTemplate;

  private static final Integer RUN_COUNT_PER_ALGORITHM = 10;

  /**
   * Solves a stable matching problem based on the given request.
   *
   * @param request request The stable matching problem configuration.
   * @return ResponseEntity
   */

  public ResponseEntity<Response> solve(StableMatchingProblemDto request) {

    try {
      log.info("Validating StableMatchingProblemDto Request ...");
      MatchingProblem problem = StableMatchingProblemMapper.toOTM(request);
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
              request.getDistributedCores());

      if (Objects.isNull(results)) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response
                        .builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Error solving OTM stable matching problem.")
                        .data(null)
                        .build());
      }

      long endTime = System.currentTimeMillis();

      double runtime = ((double) (endTime - startTime) / 1000);
      runtime = (runtime * 1000.0);
      log.info("Runtime: {} Millisecond(s).", runtime);
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
    } catch (Exception e) {
      log.error("[Service] Stable Matching: Error solving stable matching problem: {}",
              e.getMessage(),
              e);

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

    return matchingSolution;
  }

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
  private NondominatedPopulation solveProblem(Problem problem,
                                              String algorithm,
                                              int populationSize,
                                              int generation,
                                              int maxTime,
                                              String distributedCores) {
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


    try {
      if (distributedCores.equals("all")) {
        result = new Executor()
                .withProblem(problem)
                .withAlgorithm(algorithm)
                .withMaxEvaluations(generation * populationSize)
                .withTerminationCondition(maxEval)
                .withProperties(properties)
                .withProperty("operator", "CustomVariation")
                .withProperty("CustomVariation.crossoverRate", 0.9)
                .withProperty("CustomVariation.mutationRate", 0.1)

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
                .withProperty("operator", "CustomVariation")
                .withProperty("CustomVariation.crossoverRate", 0.9)
                .withProperty("CustomVariation.mutationRate", 0.1)

                .distributeOn(numberOfCores)
                .run();
      }
      //log.info("[Service] Stable Matching: Problem solved successfully!");
      return result;
    } catch (Exception e) {
      log.error("Error solving {}, {}", problem.getName(), e.getMessage(), e);
      return null;
    }
  }

  /**
   * getInsights.
   *
   * @param request     StableMatchingProblemDto
   * @param sessionCode String
   * @return ResponseEntity
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
    // solve the problem with different algorithms
    // and then evaluate the performance of the algorithms
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
                request.getPopulationSize(),
                request.getGeneration(),
                request.getMaxTime(),
                request.getDistributedCores());

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

}
