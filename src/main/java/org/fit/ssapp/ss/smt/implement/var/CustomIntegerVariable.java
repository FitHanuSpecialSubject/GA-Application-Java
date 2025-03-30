package org.fit.ssapp.ss.smt.implement.var;

import org.moeaframework.core.variable.RealVariable;

/**
 * CustomIntegerVariable, this class is created mainly to avoid automatically copy() and randomize()
 * method of BinaryIntegerVariable class.
 */
public class CustomIntegerVariable extends RealVariable {

  /**
   * CustomIntegerVariable.
   *
   * @param value is value of the variable
   * @param lowerBound double
   * @param upperBound double
   */
  public CustomIntegerVariable(double value, double lowerBound, double upperBound) {
    super(lowerBound, upperBound);
    setValue(value);
  }

  /**
   * CustomIntegerVariable.
   *
   * @param lowerBound int
   * @param upperBound int
   */
  public CustomIntegerVariable(double lowerBound, double upperBound) {
    super(lowerBound, upperBound);
  }

  @Override
  public void randomize() {
  }

  @Override
  public CustomIntegerVariable copy() {
    return new CustomIntegerVariable(getValue(), getLowerBound(), getUpperBound());
  }
}



