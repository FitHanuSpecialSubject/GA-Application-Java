package org.fit.ssapp.ss.smt;

import lombok.Setter;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

@Setter
public class PsoMatching extends AbstractProblem
{
  private NondominatedPopulation solutionSpace;

  public PsoMatching(int numberOfVariables, int numberOfObjectives)
  {
    super(numberOfVariables, numberOfObjectives);
  }

  @Override
  public void evaluate(Solution solution)
  {
    int index = EncodingUtils.getInt(solution.getVariable(0));

    solution.setObjective(
        0,
        solutionSpace.get(index).getObjective(0)
    );
  }

  @Override
  public Solution newSolution()
  {
    Solution solution = new Solution(numberOfVariables, 1);
    for (int i = 0; i < numberOfVariables; i++)
    {
      solution.setVariable(i, new RealVariable(0, solutionSpace.size()));
    }

    return solution;
  }
}
