package org.fit.ssapp.ss.smt.requirement;

/**
 * Interface for property requirement.
 */
public interface Requirement {

  /**
   * get type of Requirement for calculation.
   *
   * @return type (as int)
   */
  int getType();

  /**
   * get value for custom function computation.
   *
   * @return value
   */
  double getValueForFunction();

  /**
   * get default scaling based on Requirement values.
   *
   * @param propertyValue double
   * @return scale (must be from 0.0 to 1.0).
   */
  double getDefaultScaling(double propertyValue);

}