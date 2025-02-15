package org.fit.ssapp.ss.smt.requirement.impl;

import static org.fit.ssapp.util.NumberUtils.formatDouble;

import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.requirement.Requirement;

/**
 * Represents a requirement with both lower and upper bounds.
 * - The value is considered valid if it falls within the range `[lowerBound, upperBound]`.
 * - If the value is outside the range, the requirement is not satisfied (`scaling = 0.0`).
 */
public record TwoBound(double lowerBound, double upperBound) implements Requirement {

  @Override
  public int getType() {
    return StableMatchingConst.ReqTypes.TWO_BOUND;
  }

  @Override
  public double getValueForFunction() {
    return (lowerBound + upperBound) / 2;
  }

  @Override
  public double getDefaultScaling(double propertyValue) {
    if (propertyValue < lowerBound || propertyValue > upperBound
            ||
            lowerBound == upperBound) {
      return 0.0;
    } else {
      double diff = Math.abs(upperBound - lowerBound) / 2;
      double distance = Math.abs(((lowerBound + upperBound) / 2) - propertyValue);
      return (diff - distance) / diff + 1;
    }
  }

  /**
   * toString .
   */
  public String toString() {
    return "[" + formatDouble(lowerBound) + ", " + formatDouble(upperBound) + "]";
  }

}
