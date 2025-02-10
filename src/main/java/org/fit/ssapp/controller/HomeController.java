package org.fit.ssapp.controller;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.service.GameTheoryService;
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
 * Main Controller.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HomeController {

  @Autowired
  private GameTheoryService gameTheoryService;

  @Autowired
  private StableMatchingService stableMatchingSolver;

  @Autowired
  private StableMatchingOtmService otmProblemSolver;

  @Autowired
  private TripletMatchingService tripletMatchingSolver;


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
   * @param object StableMatchingProblemDto.
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/stable-matching-solver")
  public CompletableFuture<ResponseEntity<Response>> solveStableMatching(
          @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(stableMatchingSolver.solve(object));
  }

  //  @Async("taskExecutor")
  //  @PostMapping("/stable-matching-oto-solver")
  //  public CompletableFuture<ResponseEntity<Response>> solveStableMatchingOTO(
  //  @RequestBody StableMatchingProblemDto object) {
  //      return CompletableFuture
  //      .completedFuture(stableMatchingSolver.solveStableMatchingOTO(object));
  //      }

  /**
   * Solve OTM matching problem.
   *
   * @param object Matching Problem Request
   *
   * @return CompletableFuture
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
   * @param object Matching Problem Request
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/solve-triplet-matching")
  public CompletableFuture<ResponseEntity<Response>> solveTripletMatching(
      @RequestBody StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(tripletMatchingSolver.solve(object));
  }

  /**
   * Solve game theory problem service.
   *
   * @param gameTheoryProblem Matching Problem Request.
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/game-theory-solver")
  public CompletableFuture<ResponseEntity<Response>> solveGameTheory(
      @RequestBody GameTheoryProblemDto gameTheoryProblem) {
    return CompletableFuture.completedFuture(gameTheoryService.solveGameTheory(gameTheoryProblem));
  }

  /**
   * Solve game theory problem service.
   *
   * @param gameTheoryProblem Matching Problem Request.
   *
   * @param sessionCode session code runtime
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getProblemResultInsights(
      @RequestBody GameTheoryProblemDto gameTheoryProblem,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(gameTheoryService.getProblemResultInsights(
        gameTheoryProblem,
        sessionCode));
  }
  /**
   * Solve stable matching problem service.
   *
   * @param object Matching Problem Request.
   *
   * @param sessionCode session code runtime
   *
   * @return CompletableFuture
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
   * Solve stable matching OTM problem service.
   *
   * @param object Matching Problem Request.
   *
   * @param sessionCode session code runtime
   *
   * @return CompletableFuture
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
   * Solve stable matching Triplet problem service.
   *
   * @param object Matching Problem Request.
   *
   * @param sessionCode session code runtime
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/rbo-triplet-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getTripletMatchingResultInsights(
          @RequestBody StableMatchingProblemDto object,
          @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(tripletMatchingSolver.getInsights(
            object,
            sessionCode));
  }

}