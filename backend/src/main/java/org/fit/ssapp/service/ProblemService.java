package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.springframework.http.ResponseEntity;

/**
 * Base service for Matching Solver each one must have two functions 1. solve 2. getInsights
 */
public interface ProblemService {

  /**
   * solve
   *
   * @param problem StableMatchingProblemDto
   * @return ResponseEntity<Response>
   */
  ResponseEntity<Response> solve(StableMatchingProblemDto problem);

  /**
   * getInsights
   *
   * @param problem     StableMatchingProblemDto
   * @param sessionCode String
   * @return ResponseEntity<Response>
   */
  ResponseEntity<Response> getInsights(StableMatchingProblemDto problem, String sessionCode);
}
