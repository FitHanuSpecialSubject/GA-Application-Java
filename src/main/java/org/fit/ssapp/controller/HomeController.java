package org.fit.ssapp.controller;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main Controller for handling all API endpoints.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HomeController {

  private final GameTheoryService gameTheoryService;
  private final ExecutorService executorService;

  @Autowired
  private StableMatchingService stableMatchingSolver;

  @Autowired
  private StableMatchingOtmService otmProblemSolver;

  @Autowired
  private TripletMatchingService tripletMatchingSolver;

  @Autowired
  private PsoCompatSmtService psoCompatSmtService;

  public HomeController(GameTheoryService gameTheoryService) {
    this.gameTheoryService = gameTheoryService;
    this.executorService = Executors.newFixedThreadPool(10);
  }

  /**
   * Status check serverside page.
   *
   * @return index static page.
   */
  @GetMapping("/")
  public String home() {
    return "index";
  }

  /**
   * Solve MTM matching problem.
   *
   * @param object StableMatchingProblemDto containing the problem to solve.
   * @return CompletableFuture containing the response with solution.
   */
  @Async("taskExecutor")
  @PostMapping("/stable-matching-solver")
  public CompletableFuture<ResponseEntity<Response>> solveStableMatching(
          @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(stableMatchingSolver.solve(object));
  }

  /**
   * Solve OTM matching problem.
   *
   * @param object StableMatchingProblemDto containing the problem to solve.
   * @return CompletableFuture containing the response with solution.
   */
  @Async("taskExecutor")
  @PostMapping("/stable-matching-otm-solver")
  public CompletableFuture<ResponseEntity<Response>> solveStableMatchingOTM(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(otmProblemSolver.solve(object));
  }

  /**
   * Solve triplet (3 set) matching problem.
   *
   * @param object StableMatchingProblemDto containing the problem to solve.
   * @return CompletableFuture containing the response with solution.
   */
  @Async("taskExecutor")
  @PostMapping("/solve-triplet-matching")
  public CompletableFuture<ResponseEntity<Response>> solveTripletMatching(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(tripletMatchingSolver.solve(object));
  }

  /**
   * Solve game theory problem.
   *
   * @param dto GameTheoryProblemDto containing the problem to solve.
   * @return CompletableFuture containing the response with solution.
   */
  @Async("taskExecutor")
  @PostMapping("/game-theory-solver")
  public CompletableFuture<ResponseEntity<?>> solveGameTheory(
      @RequestBody @Valid GameTheoryProblemDto dto) {
    return CompletableFuture.completedFuture(gameTheoryService.solveGameTheory(dto));
  }

  /**
   * Get insights for a solved game theory problem.
   *
   * @param gameTheoryProblem The problem to get insights for.
   * @param sessionCode The session code for the problem.
   * @return CompletableFuture containing the response with insights.
   */
  @Async("taskExecutor")
  @PostMapping("/problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getProblemResultInsights(
      @RequestBody @Valid GameTheoryProblemDto gameTheoryProblem,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(gameTheoryService.getProblemResultInsights(
        gameTheoryProblem,
        sessionCode));
  }

  /**
   * Get insights for a solved stable matching problem.
   *
   * @param object The problem to get insights for.
   * @param sessionCode The session code for the problem.
   * @return CompletableFuture containing the response with insights.
   */
  @Async("taskExecutor")
  @PostMapping("/matching-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getMatchingResultInsights(
          @RequestBody @Valid StableMatchingProblemDto object,
          @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(stableMatchingSolver.getInsights(object,
            sessionCode));
  }

  /**
   * Get insights for a solved OTM matching problem.
   *
   * @param object The problem to get insights for.
   * @param sessionCode The session code for the problem.
   * @return CompletableFuture containing the response with insights.
   */
  @Async("taskExecutor")
  @PostMapping("/otm-matching-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getOTMMatchingResultInsights(
          @RequestBody @Valid StableMatchingProblemDto object,
          @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(otmProblemSolver.getInsights(
            object,
            sessionCode));
  }

  /**
   * Get insights for a solved triplet matching problem.
   *
   * @param object The problem to get insights for.
   * @param sessionCode The session code for the problem.
   * @return CompletableFuture containing the response with insights.
   */
  @Async("taskExecutor")
  @PostMapping("/rbo-triplet-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getTripletMatchingResultInsights(
          @RequestBody @Valid StableMatchingProblemDto object,
          @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(tripletMatchingSolver.getInsights(
            object,
            sessionCode));
  }

  /**
   * Solve stable matching problem using PSO compatibility algorithm.
   *
   * @param object StableMatchingProblemDto containing the problem to solve.
   * @return CompletableFuture containing the response with solution.
   */
  @Async("taskExecutor")
  @PostMapping("/smt-pso-compat-solve")
  public CompletableFuture<ResponseEntity<Response>> solvePsoCompatSmt(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(psoCompatSmtService.solve(object));
  }

  /**
   * Get insights for a solved PSO compatibility stable matching problem.
   *
   * @param object The problem to get insights for.
   * @param sessionCode The session code for the problem.
   * @return CompletableFuture containing the response with insights.
   */
  @Async("taskExecutor")
  @PostMapping("/smt-pso-compat-insight/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getPsoCompatSmtResultInsight(
      @RequestBody @Valid StableMatchingProblemDto object,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(psoCompatSmtService.getInsights(
        object,
        sessionCode));
  }
}