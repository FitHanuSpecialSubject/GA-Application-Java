package org.fit.ssapp.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.request.functioncheck.GtFunctionCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmEvaluateCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmFitnessCheckRequest;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.service.FunctionCheckService;
import org.fit.ssapp.service.FunctionInputCatalogService;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints to test payoff / evaluate / fitness functions on data, without running MOEA.
 * Request DTOs are deliberately not bean-validated so that partial data can be tried.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/function-testing", "/api/function-check"})
@CrossOrigin(origins = "*", maxAge = 3600)
public class FunctionCheckController {

  private final FunctionCheckService checkService;
  private final FunctionInputCatalogService catalogService;

  /** Input points of a game theory problem. */
  @PostMapping("/gt/inputs")
  public ResponseEntity<Response> gtInputs(@RequestBody(required = false) List<NormalPlayer> players) {
    return run("Game theory inputs", () -> catalogService.describeGameTheory(players));
  }

  /** Input points of a stable matching problem. */
  @PostMapping("/sm/inputs")
  public ResponseEntity<Response> smInputs(@RequestBody StableMatchingProblemDto problem) {
    return run("Stable matching inputs", () -> catalogService.describeStableMatching(problem));
  }

  /** Test GT payoff functions of a strategy profile and the fitness function. */
  @PostMapping("/gt")
  public ResponseEntity<Response> gtCheck(@RequestBody GtFunctionCheckRequest request) {
    return run("Game theory function check", () -> checkService.checkGameTheory(request));
  }

  /** Test a stable matching evaluate function (P / W / R variables). */
  @PostMapping("/sm/evaluate")
  public ResponseEntity<Response> smEvaluate(@RequestBody SmEvaluateCheckRequest request) {
    return run("Stable matching evaluate function check",
        () -> checkService.checkSmEvaluate(request));
  }

  /** Test a stable matching fitness function (M / S / SIGMA) on a matching. */
  @PostMapping("/sm/fitness")
  public ResponseEntity<Response> smFitness(@RequestBody SmFitnessCheckRequest request) {
    return run("Stable matching fitness function check",
        () -> checkService.checkSmFitness(request));
  }

  private ResponseEntity<Response> run(String name, java.util.function.Supplier<Object> action) {
    try {
      Object data = action.get();
      return ResponseEntity.ok(Response.builder()
          .status(HttpStatus.OK.value())
          .message(name + " done")
          .data(data)
          .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Response.builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .message(e.getMessage())
          .build());
    } catch (Exception e) {
      log.error("{} failed", name, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.builder()
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .message(String.valueOf(e.getMessage()))
          .build());
    }
  }
}
