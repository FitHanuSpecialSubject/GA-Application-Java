package org.fit.ssapp.controller;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.service.GameTheorySolver;
import org.fit.ssapp.service.OTMStableMatchingSolver;
import org.fit.ssapp.service.StableProblemService;
import org.fit.ssapp.service.TripletProblemRBO;
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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HomeController {

  @Autowired
  private GameTheorySolver gameTheorySolver;

  @Autowired
  private StableProblemService stableMatchingSolver;

  @Autowired
  private OTMStableMatchingSolver stableMatchingOTMProblemDTO;

  @Autowired
  private TripletProblemRBO tripletProblemRBO;


  @GetMapping("/")
  public String home() {
    return "index";
  }

  @Async("taskExecutor")
  @PostMapping("/stable-matching-solver")
  public CompletableFuture<ResponseEntity<Response>> solveStableMatching(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(stableMatchingSolver.solve(object));
  }

//    @Async("taskExecutor")
//    @PostMapping("/stable-matching-oto-solver")
//    public CompletableFuture<ResponseEntity<Response>> solveStableMatchingOTO(@RequestBody StableMatchingProblemDto object) {
//        return CompletableFuture.completedFuture(stableMatchingSolver.solveStableMatchingOTO(object));
//    }

  @Async("taskExecutor")
  @PostMapping("/stable-matching-otm-solver")
  public CompletableFuture<ResponseEntity<Response>> solveStableMatchingOTM(
      @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(stableMatchingOTMProblemDTO.solve(object));
  }

  @Async("taskExecutor")
  @PostMapping("/solve-triplet-matching")
  public CompletableFuture<ResponseEntity<Response>> solveTripletMatching(
      @RequestBody StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(tripletProblemRBO.solve(object));
  }

  @Async("taskExecutor")
  @PostMapping("/game-theory-solver")
  public CompletableFuture<ResponseEntity<Response>> solveGameTheory(
      @RequestBody GameTheoryProblemDto gameTheoryProblem) {
    return CompletableFuture.completedFuture(gameTheorySolver.solveGameTheory(gameTheoryProblem));
  }

  @Async("taskExecutor")
  @PostMapping("/problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getProblemResultInsights(
      @RequestBody GameTheoryProblemDto gameTheoryProblem,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(gameTheorySolver.getProblemResultInsights(
        gameTheoryProblem,
        sessionCode));
  }

  @Async("taskExecutor")
  @PostMapping("/matching-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getMatchingResultInsights(
      @RequestBody @Valid StableMatchingProblemDto object,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(stableMatchingSolver.getInsights(object,
        sessionCode));
  }

  @Async("taskExecutor")
  @PostMapping("/otm-matching-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getOTMMatchingResultInsights(
      @RequestBody @Valid StableMatchingProblemDto object,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(stableMatchingOTMProblemDTO.getInsights(
        object,
        sessionCode));
  }

  @Async("taskExecutor")
  @PostMapping("/rbo-triplet-problem-result-insights/{sessionCode}")
  public CompletableFuture<ResponseEntity<Response>> getTripletMatchingResultInsights(
      @RequestBody StableMatchingProblemDto object,
      @PathVariable String sessionCode) {
    return CompletableFuture.completedFuture(tripletProblemRBO.getInsights(
        object,
        sessionCode));
  }

}