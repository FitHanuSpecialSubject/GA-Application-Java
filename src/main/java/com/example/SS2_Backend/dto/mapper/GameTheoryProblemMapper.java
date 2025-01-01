package com.example.SS2_Backend.dto.mapper;

import com.example.SS2_Backend.dto.request.GameTheoryProblemDTO;
import com.example.SS2_Backend.ss.gt.GameTheoryProblem;
import com.example.SS2_Backend.ss.gt.implement.PSOCompatibleGameTheoryProblem;
import com.example.SS2_Backend.ss.gt.implement.StandardGameTheoryProblem;
import com.example.SS2_Backend.util.EvaluatorUtils;
import com.example.SS2_Backend.util.StringUtils;
import com.example.SS2_Backend.constants.AppConst;

public class GameTheoryProblemMapper {

    /**
     * Map from request to problem
     *
     * @param request GameTheoryProblemDTO
     * @return GameTheoryProblem
     */
    public static GameTheoryProblem toProblem(GameTheoryProblemDTO request) {
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
    public static StandardGameTheoryProblem toStandardProblem(PSOCompatibleGameTheoryProblem problem) {
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
