package org.fit.ssapp.ss.smt.requirement.impl;

import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.requirement.Requirement;

/**
 * ScaleTarget
 */
public record ScaleTarget(int targetValue) implements Requirement {

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return StableMatchingConst.ReqTypes.SCALE_TARGET;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getValueForFunction() {
    return this.targetValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDefaultScaling(double propertyValue) {
    if (propertyValue < 0 || propertyValue > 10) {
      return 0.0;
    } else {
      double distance = Math.abs(propertyValue - this.targetValue);
      if (distance > 7) {
        return 0;
      }
      if (distance > 5) {
        return 1;
      }
      return (10 - distance) / 10 + 1;
    }
  }

  @Override
  public String toString() {
    return "[" + this.targetValue + "]";
  }

}
