package org.fit.ssapp.controller;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.JobService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Asynchronous variants of the solver endpoints in {@link HomeController}.
 * POST returns a job id immediately, then the client polls GET until the job is finished.
 * Each POST path mirrors the synchronous endpoint (for example /api/game-theory-solver
 * becomes /api/jobs/game-theory-solver) and runs the same service.
 */
@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class JobController {

  private final JobService jobService;
  private final GameTheoryService gameTheoryService;
  private final StableMatchingService stableMatchingSolver;
  private final StableMatchingOtmService otmProblemSolver;
  private final TripletMatchingService tripletMatchingSolver;
  private final PsoCompatSmtService psoCompatSmtService;

  /**
   * Submit a game theory problem.
   *
   * @param dto problem request.
   *
   * @return job id.
   */
  @PostMapping("/game-theory-solver")
  public ResponseEntity<Response> submitGameTheory(
      @RequestBody @Valid GameTheoryProblemDto dto) {
    return submit(() -> gameTheoryService.solveGameTheory(dto));
  }

  /**
   * Submit a many to many matching problem.
   *
   * @param dto problem request.
   *
   * @return job id.
   */
  @PostMapping("/stable-matching-solver")
  public ResponseEntity<Response> submitStableMatching(
      @RequestBody @Valid StableMatchingProblemDto dto) {
    return submit(() -> stableMatchingSolver.solve(dto));
  }

  /**
   * Submit a one to many matching problem.
   *
   * @param dto problem request.
   *
   * @return job id.
   */
  @PostMapping("/stable-matching-otm-solver")
  public ResponseEntity<Response> submitStableMatchingOtm(
      @RequestBody @Valid StableMatchingProblemDto dto) {
    return submit(() -> otmProblemSolver.solve(dto));
  }

  /**
   * Submit a triplet (3 set) matching problem.
   *
   * @param dto problem request.
   *
   * @return job id.
   */
  @PostMapping("/solve-triplet-matching")
  public ResponseEntity<Response> submitTripletMatching(
      @RequestBody @Valid StableMatchingProblemDto dto) {
    return submit(() -> tripletMatchingSolver.solve(dto));
  }

  /**
   * Submit a PSO compatible many to many matching problem.
   *
   * @param dto problem request.
   *
   * @return job id.
   */
  @PostMapping("/smt-pso-compat-solve")
  public ResponseEntity<Response> submitPsoCompatSmt(
      @RequestBody @Valid StableMatchingProblemDto dto) {
    return submit(() -> psoCompatSmtService.solve(dto));
  }

  /**
   * Poll a job. While running, data holds jobId and status. Once finished, data also holds
   * "result", which is exactly the body the synchronous endpoint would have returned.
   *
   * @param jobId job id returned by one of the POST endpoints.
   *
   * @return job state.
   */
  @GetMapping("/{jobId}")
  public ResponseEntity<Response> getJob(@PathVariable String jobId) {
    return jobService.find(jobId)
        .map(job -> {
          Map<String, Object> data = new LinkedHashMap<>();
          data.put("jobId", job.getId());
          data.put("status", job.getStatus().name());
          if (job.getResult() != null) {
            data.put("result", job.getResult());
          }
          return ResponseEntity.ok(
              Response.builder().status(200).message("Job state").data(data).build());
        })
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Response.builder().status(404).message("Job not found or expired").build()));
  }

  private ResponseEntity<Response> submit(Supplier<ResponseEntity<Response>> task) {
    return jobService.submit(task)
        .map(job -> ResponseEntity.accepted().body(Response.builder()
            .status(202)
            .message("Job accepted")
            .data(Map.of("jobId", job.getId(), "status", job.getStatus().name()))
            .build()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            Response.builder()
                .status(503)
                .message("Server is busy, please try again in a few minutes")
                .build()));
  }
}
