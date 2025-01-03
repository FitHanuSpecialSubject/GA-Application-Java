package org.fit.ssapp.ss.smt.requirement.impl;

import static org.fit.ssapp.util.NumberUtils.formatDouble;

import java.util.Objects;
import lombok.Getter;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.requirement.Requirement;

@Getter
public class OneBound implements Requirement {

  @Getter
  private final double bound;
  private final boolean expression;
  private final boolean INCREASING = true;
  private final boolean DECREASING = false;

  public OneBound(double bound, boolean expression) {
    this.bound = bound;
    this.expression = expression;
  }

  /**
   * Expression (increasing/ decreasing) to String
   *
   * @return String
   */
  private String expressionToString(boolean expression) {
    return expression ? "++" : "--";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return StableMatchingConst.ReqTypes.ONE_BOUND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getValueForFunction() {
    return this.bound;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDefaultScaling(double propertyValue) {
    if (Objects.equals(this.expression, INCREASING)) {
      if (propertyValue < bound) {
        return 0.0;
      } else {
          if (bound == 0) {
              return 2.0;
          }
        double distance = Math.abs(propertyValue - bound);
        return (bound + distance) / bound;
      }
    } else {
      if (propertyValue > bound) {
        return 0.0;
      } else {
          if (bound == 0) {
              return 2.0;
          }
        double distance = Math.abs(propertyValue - bound);
        return (bound + distance) / bound;
      }
    }
  }

  public String toString() {
    return "[" + formatDouble(bound) + ", " + expressionToString(expression) + "]";
  }

}
