package org.fit.ssapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Progress;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.ss.smt.result.MatchingSolution;
import org.fit.ssapp.ss.smt.result.MatchingSolutionInsights;
import org.fit.ssapp.util.ComputerSpecsUtil;
import org.fit.ssapp.util.ResponseUtils;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;
import org.moeaframework.core.termination.MaxFunctionEvaluations;
import org.moeaframework.util.TypedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * StableMatchingService - Provides stable matching problem-solving services.
 * This service handles the execution of stable matching algorithms using different approaches.
 * It integrates with **MOEA Framework** for multi-objective optimization and allows:
 * - Solving a stable matching problem using various algorithms.
 * - Benchmarking multiple algorithms to compare performance.
 * - Real-time progress tracking and updates via WebSockets.
 * ## Main Features
 * - Solves stable matching problems based on provided configurations.
 * - Supports parallel execution using multiple computing cores.
 * - Collects performance insights for different algorithms.
 * - Handles WebSocket communication to send progress updates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StableMatchingService implements ProblemService {

  @Autowired
  private final SmtCommonService commonService;

  /**
   * Solve SMT OTM problem type
   * use SmtCommonService
   *
   * @param request StableMatchingProblemDto
   * @return ResponseEntity
   */
  public ResponseEntity<Response> solve(StableMatchingProblemDto request) {
    MatchingProblem problem;
    try {
      problem = StableMatchingProblemMapper.toMTM(request);
    } catch (Exception e) {
      String errMessage = e.getMessage();
      log.error("ERROR, error when trying to convert dto {}", errMessage, e);
      return ResponseUtils.getInternalErrorResponse(errMessage);
    }
    return this.commonService.solve(request, problem);
  }

  /**
   * Get insight session for problem type SMT MTM
   * use SmtCommonService
   *
   * @param request StableMatchingProblemDto
   * @param sessionCode String
   * @return ResponseEntity
   */
  @Override
  public ResponseEntity<Response> getInsights(StableMatchingProblemDto request,
      String sessionCode) {
    MatchingProblem problem;
    try {
      problem = StableMatchingProblemMapper.toMTM(request);
    } catch (Exception e) {
      String errMessage = e.getMessage();
      log.error("ERROR, error when trying to convert dto {}", errMessage, e);
      return ResponseUtils.getInternalErrorResponse(errMessage);
    }
    return this.commonService.getInsights(request, problem, sessionCode);
  }

}
