package org.fit.ssapp.ss.smt.requirement.impl;

import static org.fit.ssapp.util.NumberUtils.formatDouble;

import lombok.Getter;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.requirement.Requirement;

@Getter
public class TwoBound implements Requirement {

  private final double lowerBound;
  private final double upperBound;

  public TwoBound(double lowerBound, double upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return StableMatchingConst.ReqTypes.TWO_BOUND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getValueForFunction() {
    return (lowerBound + upperBound) / 2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDefaultScaling(double propertyValue) {
    if (propertyValue < lowerBound || propertyValue > upperBound ||
        lowerBound == upperBound) {
      return 0.0;
    } else {
      double diff = Math.abs(upperBound - lowerBound) / 2;
      double distance = Math.abs(((lowerBound + upperBound) / 2) - propertyValue);
      return (diff - distance) / diff + 1;
    }
  }

  public String toString() {
    return "[" + formatDouble(lowerBound) + ", " + formatDouble(upperBound) + "]";
  }

}
