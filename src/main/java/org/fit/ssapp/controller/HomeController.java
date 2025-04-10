package org.fit.ssapp.controller;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Controller.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HomeController {

  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private GameTheoryService gameTheoryService;

  @Autowired
  private StableMatchingService stableMatchingSolver;

  @Autowired
  private StableMatchingOtmService otmProblemSolver;

  @Autowired
  private TripletMatchingService tripletMatchingSolver;

  @Autowired
  private PsoCompatSmtService psoCompatSmtService;

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
      @RequestBody(required = true) @Valid GameTheoryProblemDto request) {
    try {
      // Empty JSON check - This will be processed synchronously with bad request
      if (isEmptyRequest(request)) {
        return CompletableFuture.completedFuture(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Request body is required")
                    .build()));
      }

      if (request.getNormalPlayers() == null || request.getNormalPlayers().isEmpty()) {
        return CompletableFuture.completedFuture(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Request body is invalid: normalPlayers is required")
                    .build()));
      }

      return CompletableFuture.completedFuture(gameTheoryService.solveGameTheory(request));
    } catch (Exception e) {
      log.error("Error processing request: {}", e.getMessage());
      return CompletableFuture.completedFuture(
          ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(Response.builder()
                  .status(HttpStatus.BAD_REQUEST.value())
                  .message(e.getMessage())
                  .build()));
    }
  }

  /**
   * Helper method to check if a request is effectively empty
   */
  private boolean isEmptyRequest(GameTheoryProblemDto request) {
    if (request == null) {
      return true;
    }
    
    // Check if it's an empty JSON object '{}'
    // For an empty object, all these fields will be null
    return request.getNormalPlayers() == null && 
           request.getSpecialPlayer() == null && 
           request.getDefaultPayoffFunction() == null && 
           request.getFitnessFunction() == null &&
           request.getAlgorithm() == null;
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


  /**
   * solveStableMatchingOTM
   *
   * @param object Matching Problem Request
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/smt-pso-compat-solve")
  public CompletableFuture<ResponseEntity<Response>> solvePsoCompatSmt(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(psoCompatSmtService.solve(object));
  }



  /**
   * get stable matching PsoCompatSmtResultInsight
   *
   * @param object Matching Problem Request.
   *
   * @param sessionCode session code runtime
   *
   * @return CompletableFuture
   */
  @Async("taskExecutor")
  @PostMapping("/smt-pso-compat-insight/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getPsoCompatSmtResultInsight(
      @RequestBody StableMatchingProblemDto object,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(psoCompatSmtService.getInsights(
        object,
        sessionCode));
  }

}