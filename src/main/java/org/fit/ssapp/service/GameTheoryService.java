package org.fit.ssapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.dto.mapper.GameTheoryProblemMapper;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.response.Progress;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.implement.PsoCompatibleGameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.StandardGameTheoryProblem;
import org.fit.ssapp.ss.gt.result.GameSolution;
import org.fit.ssapp.ss.gt.result.GameSolutionInsights;
import org.fit.ssapp.util.NumberUtils;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import jakarta.validation.ValidationException;
import org.fit.ssapp.util.StringExpressionEvaluator;

/**
 * Service class for solving game theory problems and providing insights into algorithm performance.
 * This class handles the execution of algorithms, formatting of solutions, and communication of
 * progress.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameTheoryService {

  private final SimpMessagingTemplate simpMessagingTemplate;

  private static final int RUN_COUNT_PER_ALGORITHM = 10;

  /**
   * Solves a game theory problem using the specified algorithm and returns the solution.
   *
   * @param request the game theory problem request DTO
   * @return a ResponseEntity containing the solution or an error message
   */
  public ResponseEntity<?> solveGameTheory(GameTheoryProblemDto request) {
    try {
      // Validate request is not null and has required fields
      if (request == null) {
        return ResponseEntity
            .badRequest()
            .body(Map.of("error", "Request body is required"));
      }

      if (request.getNormalPlayers() == null || request.getNormalPlayers().isEmpty()) {
        return ResponseEntity
            .badRequest()
            .body(Map.of("error", "Request body is invalid: normalPlayers is required"));
      }

      log.info("Received request: {}", request);

      GameTheoryProblem problem = GameTheoryProblemMapper.toProblem(request);

      long startTime = System.currentTimeMillis();
      log.info("Running algorithm:  {}...", request.getAlgorithm());
      NondominatedPopulation results;
      results = solveProblem(problem,
          request.getAlgorithm(),
          request.getGeneration(),
          request.getPopulationSize(),
          request.getDistributedCores(),
          request.getMaxTime());
      long endTime = System.currentTimeMillis();
      double runtime = ((double) (endTime - startTime) / 1000 / 60);
      runtime = Math.round(runtime * 100.0) / 100.0;

      log.info("Algorithm: {} finished in {} minutes", request.getAlgorithm(), runtime);

      // format the output
      log.info("Preparing the solution ...");
      GameSolution gameSolution = formatSolution(problem, results);
      gameSolution.setAlgorithm(request.getAlgorithm());
      gameSolution.setRuntime(runtime);

      Response response = Response.builder()
          .status(200)
          .message("Success")
          .data(gameSolution)
          .build();

      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(response);
    } catch (IllegalArgumentException | ValidationException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Error ", e);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Solves the game theory problem using the specified algorithm and parameters.
   *
   * @param problem          the game theory problem to solve
   * @param algorithm        the algorithm to use for solving the problem
   * @param generation       the number of generations
   * @param populationSize   the population size
   * @param distributedCores the number of cores to distribute the computation
   * @param maxTime          the maximum time allowed for computation
   * @return the nominated population of solutions
   */
  private NondominatedPopulation solveProblem(GameTheoryProblem problem,
      String algorithm,
      Integer generation,
      Integer populationSize,
      String distributedCores,
      Integer maxTime) {

    NondominatedPopulation results;
    try {
      if (distributedCores.equals("all")) {
        results = new Executor()
            .withProblem(problem)
            .withAlgorithm(algorithm)
            .withMaxEvaluations(generation
                *
                populationSize)
            .withProperty("populationSize", populationSize)
            .withProperty("maxTime", maxTime)
            .distributeOnAllCores()
            .run();

      } else {
        int numberOfCores = Integer.parseInt(distributedCores);
        results = new Executor()
            .withProblem(problem)
            .withAlgorithm(algorithm)
            .withMaxEvaluations(generation
                *
                populationSize)
            .withProperty("populationSize", populationSize)
            .withProperty("maxTime", maxTime)
            .distributeOn(numberOfCores)
            .run();
      }
      return results;
    } catch (Exception e) {

      // second attempt to solve the problem if the first run got some error
      if (distributedCores.equals("all")) {
        results = new Executor()
            .withProblem(problem)
            .withAlgorithm(algorithm)
            .withMaxEvaluations(generation
                *
                populationSize)
            .withProperty("populationSize", populationSize)
            .withProperty("maxTime", maxTime)
            .distributeOnAllCores()
            .run();


      } else {
        int numberOfCores = Integer.parseInt(distributedCores);
        results = new Executor()
            .withProblem(problem)
            .withAlgorithm(algorithm)
            .withMaxEvaluations(generation * populationSize)
            .withProperty("populationSize", populationSize)
            .withProperty("maxTime", maxTime)
            .distributeOn(numberOfCores)
            .run();
      }
      return results;


    }
  }

  /**
   * Formats the solution of the game theory problem into a user-friendly format.
   *
   * @param problem the game theory problem
   * @param result  the nominated population of solutions
   * @return the formatted game solution
   */
  public static GameSolution formatSolution(GameTheoryProblem problem,
      NondominatedPopulation result) {
    Solution solution = result.get(0);
    GameSolution gameSolution = new GameSolution();

    double fitnessValue = solution.getObjective(0);
    gameSolution.setFitnessValue(fitnessValue);

    List<NormalPlayer> players = problem.getNormalPlayers();
    List<GameSolution.Player> gameSolutionPlayers = new ArrayList<>();

    int chosenStratIdx;
    // loop through all players and get the strategy chosen by each player
    for (int i = 0; i < solution.getNumberOfVariables(); i++) {
      NormalPlayer normalPlayer = players.get(i);

      Variable var = solution.getVariable(i);
      if (var instanceof RealVariable) {
        chosenStratIdx = NumberUtils.toInteger((RealVariable) var);
      } else if (var instanceof BinaryIntegerVariable) {
        chosenStratIdx = EncodingUtils.getInt(var);
      } else {
        // :v
        chosenStratIdx = EncodingUtils.getInt(var);
      }

      double strategyPayoff = normalPlayer.getStrategyAt(chosenStratIdx).getPayoff();

      String playerName = getPlayerName(normalPlayer, i);
      String strategyName = getStrategyName(chosenStratIdx, normalPlayer, i);

      GameSolution.Player gameSolutionPlayer = GameSolution.Player
          .builder()
          .playerName(playerName)
          .strategyName(strategyName)
          .payoff(strategyPayoff)
          .build();

      gameSolutionPlayers.add(gameSolutionPlayer);

    }

    gameSolution.setPlayers(gameSolutionPlayers);

    return gameSolution;
  }

  /**
   * Retrieves insights into the performance of different algorithms for solving the game theory
   * problem.
   *
   * @param request     the game theory problem request DTO
   * @param sessionCode the session code for progress communication
   * @return a ResponseEntity containing the insights or an error message
   */
  public ResponseEntity<Response> getProblemResultInsights(GameTheoryProblemDto request,
      String sessionCode) {
    log.info("Received request:.. {}", request);
    String[] algorithms = GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;

    simpMessagingTemplate.convertAndSendToUser(sessionCode,
        "/progress",
        createProgressMessage("Initializing the problem..."));

    log.info("Mapping request to problem ...");
    GameTheoryProblem problem = GameTheoryProblemMapper.toProblem(request);
    GameSolutionInsights gameSolutionInsights = initGameSolutionInsights(algorithms);
    int runCount = 1;
    int maxRunCount = algorithms.length * RUN_COUNT_PER_ALGORITHM;

    log.info("Start benchmarking the algorithms...");
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
        "/progress",
        createProgressMessage("Start benchmarking the algorithms..."));

    for (String algorithm : algorithms) {
      log.info("Running algorithm: {}...", algorithm);
      for (int i = 0; i < RUN_COUNT_PER_ALGORITHM; i++) {
        System.out.println("Iteration: " + i);
        long start = System.currentTimeMillis();

        if (problem instanceof StandardGameTheoryProblem
            && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
          problem = GameTheoryProblemMapper
              .toPsoProblem((StandardGameTheoryProblem) problem);
        }

        if (problem instanceof PsoCompatibleGameTheoryProblem
            && !AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
          problem = GameTheoryProblemMapper
              .toStandardProblem((PsoCompatibleGameTheoryProblem) problem);
        }

        NondominatedPopulation results = solveProblem(problem,
            algorithm,
            request.getGeneration(),
            request.getPopulationSize(),
            request.getDistributedCores(),
            request.getMaxTime());

        long end = System.currentTimeMillis();

        double runtime = (double) (end - start) / 1000;
        double fitnessValue;
        fitnessValue = getFitnessValue(results);

        // send the progress to the client
        String message =
            "Algorithm " + algorithm + " finished iteration: #" + (i + 1) + "/"
                +
                RUN_COUNT_PER_ALGORITHM;
        Progress progress = createProgress(message, runtime, runCount, maxRunCount);
        System.out.println(progress);
        simpMessagingTemplate.convertAndSendToUser(sessionCode, "/progress", progress);
        runCount++;

        // add the fitness value and runtime to the insights
        gameSolutionInsights.getFitnessValues().get(algorithm).add(fitnessValue);
        gameSolutionInsights.getRuntimes().get(algorithm).add(runtime);


      }

    }
    log.info("Benchmarking finished!");
    simpMessagingTemplate.convertAndSendToUser(sessionCode,
        "/progress",
        createProgressMessage("Benchmarking finished!"));

    return ResponseEntity.ok(Response
        .builder()
        .status(200)
        .message("Get problem result insights successfully!")
        .data(gameSolutionInsights)
        .build());
  }

  /**
   * Initializes the game solution insights object with empty lists for fitness values and
   * runtimes.
   *
   * @param algorithms the list of algorithms to initialize insights for
   * @return the initialized game solution insights object
   */
  private GameSolutionInsights initGameSolutionInsights(String[] algorithms) {
    GameSolutionInsights gameSolutionInsights = new GameSolutionInsights();
    Map<String, List<Double>> fitnessValueMap = new HashMap<>();
    Map<String, List<Double>> runtimeMap = new HashMap<>();

    gameSolutionInsights.setFitnessValues(fitnessValueMap);
    gameSolutionInsights.setRuntimes(runtimeMap);

    for (String algorithm : algorithms) {
      fitnessValueMap.put(algorithm, new ArrayList<>());
      runtimeMap.put(algorithm, new ArrayList<>());
    }

    return gameSolutionInsights;
  }

  /**
   * Creates a progress object to communicate the current progress to the client.
   *
   * @param message     the progress message
   * @param runtime     the runtime of the current iteration
   * @param runCount    the current run count
   * @param maxRunCount the maximum run count
   * @return the progress object
   */
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

  /**
   * Creates a progress object with a message (no progress tracking).
   *
   * @param message the message to send
   * @return the progress object
   */
  private Progress createProgressMessage(String message) {
    return Progress
        .builder()
        .inProgress(
            false) // this object is just to send a message to the client, not to show the progress
        .message(message)
        .build();
  }

  /**
   * Retrieves the name of a player, defaulting to a formatted string if the name is null.
   *
   * @param normalPlayer the player object
   * @param index        the index of the player
   * @return the player name
   */
  public static String getPlayerName(NormalPlayer normalPlayer, int index) {
    String playerName = normalPlayer.getName();
    if (playerName == null) {
      playerName = String.format("Player %d", index);
    }

    return playerName;
  }

  /**
   * Retrieves the name of a strategy, defaulting to a formatted string if the name is null.
   *
   * @param chosenStrategyIndex the index of the chosen strategy
   * @param normalPlayer        the player object
   * @param index               the index of the player
   * @return the strategy name
   */
  public static String getStrategyName(int chosenStrategyIndex,
      NormalPlayer normalPlayer,
      int index) {
    String strategyName = normalPlayer.getStrategies().get(chosenStrategyIndex).getName();
    if (strategyName == null) {
      strategyName = String.format("Strategy %d", index);
    }

    return strategyName;
  }


  /**
   * Retrieves the fitness value from the first solution in the nominated population.
   *
   * @param result the nominated population of solutions
   * @return the fitness value
   */
  private static double getFitnessValue(NondominatedPopulation result) {

    Solution solution = result.get(0);
    return solution.getObjective(0);

  }

}
