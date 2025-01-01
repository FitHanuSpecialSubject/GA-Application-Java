package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingPrDto;
import org.fit.ssapp.dto.response.Response;
import org.springframework.http.ResponseEntity;

/**
 * Base service for Matching Solver each one must have two functions 1. solve 2. getInsights
 */
public interface ProblemService {

  /**
   * solve
   *
   * @param problem StableMatchingPrDto
   * @return ResponseEntity<Response>
   */
  ResponseEntity<Response> solve(StableMatchingPrDto problem);

  /**
   * getInsights
   *
   * @param problem     StableMatchingPrDto
   * @param sessionCode String
   * @return ResponseEntity<Response>
   */
  ResponseEntity<Response> getInsights(StableMatchingPrDto problem, String sessionCode);
}
