package com.example.SS2_Backend.service;

import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import com.example.SS2_Backend.dto.response.Response;
import org.springframework.http.ResponseEntity;

/**
 * Base service for Matching Solver
 * each one must have two functions
 * 1. solve
 * 2. getInsights
 */
public interface ProblemService {

    /**
     * solve
     *
     * @param problem StableMatchingProblemDTO
     * @return ResponseEntity<Response>
     */
    ResponseEntity<Response>  solve(StableMatchingProblemDTO problem);

    /**
     * getInsights
     *
     * @param problem StableMatchingProblemDTO
     * @param sessionCode String
     * @return ResponseEntity<Response>
     */
    ResponseEntity<Response>  getInsights(StableMatchingProblemDTO problem, String sessionCode);
}
