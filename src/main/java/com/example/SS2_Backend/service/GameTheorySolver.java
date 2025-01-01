package com.example.SS2_Backend.service;

import com.example.SS2_Backend.constants.AppConst;
import com.example.SS2_Backend.constants.GameTheoryConst;
import com.example.SS2_Backend.dto.mapper.GameTheoryProblemMapper;
import com.example.SS2_Backend.dto.request.GameTheoryProblemDTO;
import com.example.SS2_Backend.dto.response.Progress;
import com.example.SS2_Backend.dto.response.Response;
import com.example.SS2_Backend.ss.gt.GameTheoryProblem;
import com.example.SS2_Backend.ss.gt.NormalPlayer;
import com.example.SS2_Backend.ss.gt.implement.PSOCompatibleGameTheoryProblem;
import com.example.SS2_Backend.ss.gt.implement.StandardGameTheoryProblem;
import com.example.SS2_Backend.ss.gt.result.GameSolution;
import com.example.SS2_Backend.ss.gt.result.GameSolutionInsights;
import com.example.SS2_Backend.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameTheorySolver {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private static final int RUN_COUNT_PER_ALGORITHM = 10; // for insight running, each algorithm will be run for 10 times

    public ResponseEntity<Response> solveGameTheory(GameTheoryProblemDTO request) {

        try {
            log.info("Received request: " + request);
            GameTheoryProblem problem = GameTheoryProblemMapper.toProblem(request);

//            log.info("start writing {} problem to file", problem.getName());
//            boolean result = ProblemUtils.writeProblemToFile(problem, "gt_data_1");
//            if (result) {
//                log.info("finished writing {} problem to file", problem.getName());
//            } else {
//                log.info("failed writing {} problem to file", problem.getName());
//            }

            long startTime = System.currentTimeMillis();
            log.info("Running algorithm: " + request.getAlgorithm() + "...");

            // solve the problem
            NondominatedPopulation results = solveProblem(problem,
                    request.getAlgorithm(),
                    request.getGeneration(),
                    request.getPopulationSize(),
                    request.getDistributedCores(),
                    request.getMaxTime());
            long endTime = System.currentTimeMillis();
            double runtime = ((double) (endTime - startTime) / 1000 / 60);
            runtime = Math.round(runtime * 100.0) / 100.0;

            log.info("Algorithm: " + request.getAlgorithm() + " finished in " + runtime +
                    " minutes");

            // format the output
            log.info("Preparing the solution ...");
            GameSolution gameSolution = formatSolution(problem, results);
            gameSolution.setAlgorithm(request.getAlgorithm());
            gameSolution.setRuntime(runtime);
            return ResponseEntity.ok(Response
                    .builder()
                    .status(200)
                    .message("Solve game theory problem successfully!")
                    .data(gameSolution)
                    .build());
        } catch (Exception e) {
            log.error("Error ", e);
            return ResponseEntity
                    .ok()
                    .body(Response.builder().status(500).message(e.getMessage()).build());
        }
    }

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
                        .withMaxEvaluations(generation *
                                populationSize) // we are using the number of generations and population size to calculate the number of evaluations
                        .withProperty("populationSize", populationSize)
                        .withProperty("maxTime", maxTime)
                        .distributeOnAllCores()
                        .run();


            } else {
                int numberOfCores = Integer.parseInt(distributedCores);
                results = new Executor()
                        .withProblem(problem)
                        .withAlgorithm(algorithm)
                        .withMaxEvaluations(generation *
                                populationSize) // we are using the number of generations and population size to calculate the number of evaluations
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
                        .withMaxEvaluations(generation *
                                populationSize) // we are using the number of generations and population size to calculate the number of evaluations
                        .withProperty("populationSize", populationSize)
                        .withProperty("maxTime", maxTime)
                        .distributeOnAllCores()
                        .run();


            } else {
                int numberOfCores = Integer.parseInt(distributedCores);
                results = new Executor()
                        .withProblem(problem)
                        .withAlgorithm(algorithm)
                        .withMaxEvaluations(generation *
                                populationSize) // we are using the number of generations and population size to calculate the number of evaluations
                        .withProperty("populationSize", populationSize)
                        .withProperty("maxTime", maxTime)
                        .distributeOn(numberOfCores)
                        .run();
            }
            return results;


        }
    }

    public static GameSolution formatSolution(GameTheoryProblem problem, NondominatedPopulation result) {
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

    public ResponseEntity<Response> getProblemResultInsights(GameTheoryProblemDTO request,
                                                             String sessionCode) {
        log.info("Received request: " + request);
        String[] algorithms = GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;


        simpMessagingTemplate.convertAndSendToUser(sessionCode,
                "/progress",
                createProgressMessage("Initializing the problem..."));

        log.info("Mapping request to problem ...");
        GameTheoryProblem problem = GameTheoryProblemMapper.toProblem(request);
        GameSolutionInsights gameSolutionInsights = initGameSolutionInsights(algorithms);
        int runCount = 1;
        int maxRunCount = algorithms.length * RUN_COUNT_PER_ALGORITHM;
        // solve the problem with different algorithms and then evaluate the performance of the algorithms
        log.info("Start benchmarking the algorithms...");
        simpMessagingTemplate.convertAndSendToUser(sessionCode,
                "/progress",
                createProgressMessage("Start benchmarking the algorithms..."));

        for (String algorithm : algorithms) {
            log.info("Running algorithm: " + algorithm + "...");
            for (int i = 0; i < RUN_COUNT_PER_ALGORITHM; i++) {
                System.out.println("Iteration: " + i);
                long start = System.currentTimeMillis();

                if (problem instanceof StandardGameTheoryProblem
                        && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
                    problem = GameTheoryProblemMapper
                            .toPSOProblem((StandardGameTheoryProblem) problem);
                }

                if (problem instanceof PSOCompatibleGameTheoryProblem
                        && !AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
                    problem = GameTheoryProblemMapper
                            .toStandardProblem((PSOCompatibleGameTheoryProblem) problem);
                }

                NondominatedPopulation results = solveProblem(problem,
                        algorithm,
                        request.getGeneration(),
                        request.getPopulationSize(),
                        request.getDistributedCores(),
                        request.getMaxTime());

                long end = System.currentTimeMillis();

                double runtime = (double) (end - start) / 1000;
                double fitnessValue = getFitnessValue(results);

                // send the progress to the client
                String message =
                        "Algorithm " + algorithm + " finished iteration: #" + (i + 1) + "/" +
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

    private Progress createProgress(String message,
                                    Double runtime,
                                    Integer runCount,
                                    int maxRunCount) {
        int percent = runCount * 100 / maxRunCount;
        int minuteLeff = (int) Math.ceil(
                ((maxRunCount - runCount) * runtime) / 60); // runtime is in seconds
        return Progress
                .builder()
                .inProgress(true) // this object is just to send to the client to show the progress
                .message(message)
                .runtime(runtime)
                .minuteLeft(minuteLeff)
                .percentage(percent)
                .build();
    }

    private Progress createProgressMessage(String message) {
        return Progress
                .builder()
                .inProgress(false) // this object is just to send a message to the client, not to show the progress
                .message(message)
                .build();
    }


    public static String getPlayerName(NormalPlayer normalPlayer, int index) {
        String playerName = normalPlayer.getName();
        if (playerName == null) {
            playerName = String.format("Player %d", index);
        }

        return playerName;
    }

    public static String getStrategyName(int chosenStrategyIndex,
                                         NormalPlayer normalPlayer,
                                         int index) {
        String strategyName = normalPlayer.getStrategies().get(chosenStrategyIndex).getName();
        if (strategyName == null) {
            strategyName = String.format("Strategy %d", index);
        }

        return strategyName;
    }


    private static double getFitnessValue(NondominatedPopulation result) {

        Solution solution = result.get(0);
        return solution.getObjective(0);

    }

}
