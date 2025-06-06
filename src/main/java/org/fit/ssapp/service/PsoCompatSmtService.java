package org.fit.ssapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Progress;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.ss.smt.result.MatchingSolution;
import org.fit.ssapp.ss.smt.result.MatchingSolutionInsights;
import org.fit.ssapp.util.ComputerSpecsUtil;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;
import org.moeaframework.core.termination.MaxFunctionEvaluations;
import org.moeaframework.util.TypedProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PsoCompatSmtService implements ProblemService {

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

      MatchingProblem problem = StableMatchingProblemMapper.toPsoCompat(request);
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
                .message("Error solving stable matching problem.")
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
    TerminationCondition maxEval =
        new MaxFunctionEvaluations(generation * populationSize);

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
    String[] algorithms = StableMatchingConst.PSO_ALLOWED_INSIGHT_ALGORITHMS;
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
        "/progress",
        SmtCommonService.createProgressMessage("Initializing the problem..."));
    MatchingProblem problem = StableMatchingProblemMapper.toMTM(request);

    log.info("Start benchmarking {} session code {}", problem.getName(), sessionCode);

    MatchingSolutionInsights matchingSolutionInsights = SmtCommonService
        .initMatchingSolutionInsights(algorithms);

    int runCount = 1;
    int maxRunCount = algorithms.length * RUN_COUNT_PER_ALGORITHM;
    // solve the problem with different algorithms and then evaluate the performance
    // of the algorithms
    //        log.info("Start benchmarking the algorithms...");
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
        "/progress",
        SmtCommonService.createProgressMessage("Start benchmarking the algorithms..."));

    // Flag if problem is PSO Compatible instance
    boolean isConvertedToPso = false;

    for (String algorithm : algorithms) {
      for (int i = 0; i < RUN_COUNT_PER_ALGORITHM; i++) {

        if (AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
          log.info("Converting to PSO compatible instance, problem name {}", problem.getName());
          // Convert if meet algorithm condition
          if (!isConvertedToPso) {
            problem = StableMatchingProblemMapper.toPsoCompat(request);
            isConvertedToPso = true;
          }
        } else {
          log.info("Converting back to MTM problem from PSO compatible instance, problem name {}",
              problem.getName());
          // Convert back to MTM if not PSO based algorithm
          if (isConvertedToPso) {
            problem = StableMatchingProblemMapper.toMTM(request);
            isConvertedToPso = false;
          }
        }

        log.info("Iteration: {}", i);
        long start = System.currentTimeMillis();

        NondominatedPopulation results = solveProblem(problem,
            algorithm,
            request.getGeneration(),
            request.getPopulationSize(),
            request.getMaxTime(),
            request.getDistributedCores());

        long end = System.currentTimeMillis();
        assert results != null;
        double runtime = (double) (end - start) / 1000;
        double fitnessValue = SmtCommonService.getFitnessValue(results);

        // send the progress to the client
        String message =
            "Algorithm " + algorithm + " finished iteration: #" + (i + 1) + "/"
                + RUN_COUNT_PER_ALGORITHM;
        Progress progress = SmtCommonService
            .createProgress(message, runtime, runCount, maxRunCount);
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
        SmtCommonService.createProgressMessage("Benchmarking finished!"));

    return ResponseEntity.ok(Response
        .builder()
        .status(200)
        .message("Get problem result insights successfully!")
        .data(matchingSolutionInsights)
        .build());
  }
}
