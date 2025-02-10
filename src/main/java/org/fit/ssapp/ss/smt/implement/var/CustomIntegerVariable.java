package org.fit.ssapp.ss.smt.implement.var;

import org.moeaframework.core.variable.BinaryIntegerVariable;

/**
 * CustomIntegerVariable, this class is created mainly to avoid automatically copy() and randomize()
 * method of BinaryIntegerVariable class.
 */
public class CustomIntegerVariable extends BinaryIntegerVariable {

  /**
   * CustomIntegerVariable.
   *
   * @param value is value of the variable
   * @param lowerBound int
   * @param upperBound int
   */
  public CustomIntegerVariable(int value, int lowerBound, int upperBound) {
    super(lowerBound, upperBound);
    setValue(value);
  }

  /**
   * CustomIntegerVariable.
   *
   * @param lowerBound int
   * @param upperBound int
   */
  public CustomIntegerVariable(int lowerBound, int upperBound) {
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



