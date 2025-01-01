package org.fit.ssapp.dto.mapper;

import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.PSOCompatibleGameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.StandardGameTheoryProblem;
import org.fit.ssapp.util.EvaluatorUtils;
import org.fit.ssapp.util.StringUtils;

public class GameTheoryProblemMapper {

  /**
   * Map from request to problem
   *
   * @param request GameTheoryProblemDto
   * @return GameTheoryProblem
   */
  public static GameTheoryProblem toProblem(GameTheoryProblemDto request) {
    GameTheoryProblem problem;
    String algorithm = request.getAlgorithm();
    if (!StringUtils.isEmptyOrNull(algorithm)
        && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
      problem = new PSOCompatibleGameTheoryProblem();
    } else {
      problem = new StandardGameTheoryProblem();
    }
    problem.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(request.getDefaultPayoffFunction()));
    problem.setFitnessFunction(EvaluatorUtils
        .getValidFitnessFunction(request.getFitnessFunction()));
    problem.setSpecialPlayer(request.getSpecialPlayer());
    problem.setNormalPlayers(request.getNormalPlayers());
    problem.setConflictSet(request.getConflictSet());
    problem.setMaximizing(request.isMaximizing());

    return problem;
  }

  /**
   * Map from StandardGameTheoryProblem to PSOCompatibleGameTheoryProblem
   *
   * @param problem StandardGameTheoryProblem
   * @return PSOCompatibleGameTheoryProblem
   */
  public static PSOCompatibleGameTheoryProblem toPSOProblem(StandardGameTheoryProblem problem) {
    PSOCompatibleGameTheoryProblem result = new PSOCompatibleGameTheoryProblem();
    result.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(problem.getDefaultPayoffFunction()));
    result.setFitnessFunction(EvaluatorUtils
        .getValidFitnessFunction(problem.getFitnessFunction()));
    result.setSpecialPlayer(problem.getSpecialPlayer());
    result.setNormalPlayers(problem.getNormalPlayers());
    result.setConflictSet(problem.getConflictSet());
    result.setMaximizing(problem.isMaximizing());
    return result;
  }

  /**
   * Map from StandardGameTheoryProblem to PSOCompatibleGameTheoryProblem
   *
   * @param problem StandardGameTheoryProblem
   * @return PSOCompatibleGameTheoryProblem
   */
  public static StandardGameTheoryProblem toStandardProblem(
      PSOCompatibleGameTheoryProblem problem) {
    StandardGameTheoryProblem result = new StandardGameTheoryProblem();
    result.setDefaultPayoffFunction(EvaluatorUtils
        .getIfDefaultFunction(problem.getDefaultPayoffFunction()));
    result.setFitnessFunction(EvaluatorUtils
        .getValidFitnessFunction(problem.getFitnessFunction()));
    result.setSpecialPlayer(problem.getSpecialPlayer());
    result.setNormalPlayers(problem.getNormalPlayers());
    result.setConflictSet(problem.getConflictSet());
    result.setMaximizing(problem.isMaximizing());
    return result;
  }
}
