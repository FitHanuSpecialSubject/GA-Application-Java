package org.fit.ssapp.dto.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.NormalizedRandomGenerator;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
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
    problem.setNormalPlayers(toList(request.getNormalPlayers()));
    problem.setConflictSet(toList(request.getConflictSet()));
    problem.setMaximizing(request.isMaximizing());

    return problem;
  }

  private static List<NormalPlayer> toList(double[][][] playerDto) {
    List<NormalPlayer> playerList = new ArrayList<>();
    for (double[][] matrix: playerDto) {
      NormalPlayer player = new NormalPlayer();
      
      List<Strategy> stratList = new ArrayList<>();
      for (double[] strat : matrix) {
        Strategy temp = new Strategy();
        
        List<Double> props = new ArrayList(strat.length);

        for (double p : strat) {
          props.add(p);
        }

        stratList.add(temp);
      }
      

      playerList.add(player);
    }

    return playerList;
  }

  private static List<Conflict> toList(int[][] conflictDto) {
    List<Conflict> conflictList = new ArrayList<>();

    for (int[] list : conflictDto) {
      Conflict conflict = new Conflict(list[0], list[1], list[2], list[3]);
      conflictList.add(conflict);
    }

    return conflictList;
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
    result.setNormalPlayers(problem.getNormalPlayers());
    result.setConflictSet(problem.getConflictSet());
    result.setMaximizing(problem.isMaximizing());
    return result;
  }
}
