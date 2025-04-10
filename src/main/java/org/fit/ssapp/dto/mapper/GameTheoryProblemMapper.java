package org.fit.ssapp.dto.mapper;

import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.PsoCompatibleGameTheoryProblem;
import org.fit.ssapp.ss.gt.implement.StandardGameTheoryProblem;
import org.fit.ssapp.util.EvaluatorUtils;
import org.fit.ssapp.util.StringUtils;

/**
 * Mapper class for converting a GameTheoryProblemDto request into a GameTheoryProblem object. This
 * class helps in mapping data from DTO format to the appropriate game theory problem type.
 */
public class GameTheoryProblemMapper {

  /**
   * Converts a GameTheoryProblemDto request to a GameTheoryProblem object. Depending on the
   * algorithm type, it returns either a {@link PsoCompatibleGameTheoryProblem} or a
   * {@link StandardGameTheoryProblem}.
   *
   * @param request The DTO containing game theory problem details.
   * @return A mapped GameTheoryProblem object.
   */
  public static GameTheoryProblem toProblem(GameTheoryProblemDto request) {
    GameTheoryProblem problem;
    String algorithm = request.getAlgorithm();
    if (!StringUtils.isEmptyOrNull(algorithm)
        && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
      problem = new PsoCompatibleGameTheoryProblem();
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
   * Converts a StandardGameTheoryProblem to a PsoCompatibleGameTheoryProblem.
   *
   * @param problem The standard game theory problem to convert.
   * @return A PsoCompatibleGameTheoryProblem instance with copied data.
   */
  public static PsoCompatibleGameTheoryProblem toPsoProblem(StandardGameTheoryProblem problem) {
    PsoCompatibleGameTheoryProblem result = new PsoCompatibleGameTheoryProblem();
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
   * Converts a PsoCompatibleGameTheoryProblem to a StandardGameTheoryProblem.
   *
   * @param problem The PSO-compatible game theory problem to convert.
   * @return A StandardGameTheoryProblem instance with copied data.
   */
  public static StandardGameTheoryProblem toStandardProblem(
      PsoCompatibleGameTheoryProblem problem) {
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