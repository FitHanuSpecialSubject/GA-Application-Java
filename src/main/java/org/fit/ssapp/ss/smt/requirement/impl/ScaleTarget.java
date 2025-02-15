package org.fit.ssapp.ss.smt.requirement.impl;

import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.requirement.Requirement;

/**
 * Represents a requirement with a fixed target value.
 * - This class is used to evaluate how close a given value is to the `targetValue`.
 * - The scaling factor is calculated based on the distance from the target.
 * - Values outside the range `[0,10]` are considered invalid (scaling = `0.0`).
 */
public record ScaleTarget(int targetValue) implements Requirement {

  @Override
  public int getType() {
    return StableMatchingConst.ReqTypes.SCALE_TARGET;
  }

  @Override
  public double getValueForFunction() {
    return this.targetValue;
  }

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
