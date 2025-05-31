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
import org.fit.ssapp.ss.smt.implement.OTMProblem;
import org.fit.ssapp.ss.smt.result.MatchingSolution;
import org.fit.ssapp.ss.smt.result.MatchingSolutionInsights;
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
 * StableMatchingOtmService - Provides stable matching problem-solving services.
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
public class StableMatchingOtmService {

  @Autowired
  private final SmtCommonService commonService;

  /**
   * Solves a stable matching problem based on the given request.
   *
   * @param request request The stable matching problem configuration.
   * @return ResponseEntity
   */

  public ResponseEntity<Response> solve(StableMatchingProblemDto request) {
    log.info("Parsing OTM problem from request");
    MatchingProblem problem = StableMatchingProblemMapper.toOTM(request);
    return this.commonService.solve(request, problem);
  }

  /**
   * getInsights.
   *
   * @param request     StableMatchingProblemDto
   * @param sessionCode String
   * @return ResponseEntity
   */
  public ResponseEntity<Response> getInsights(StableMatchingProblemDto request,
                                              String sessionCode) {
    OTMProblem problem = StableMatchingProblemMapper.toOTM(request);
    return this.commonService.getInsights(request, problem, sessionCode);
  }

}
