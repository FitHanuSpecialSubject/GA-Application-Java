package org.fit.ssapp.constants;

/**
 * Message constants.
 */
public class MessageConst {

  /**
   * Error messages.
   */
  public interface ErrMessage {

    /**
     * validate number of set.
     */
    String MES_001 = "Number of set must be greater or equal to 2";
    /**
     * validate number of individualNumber.
     */
    String MES_002 = "The number of individuals (or corresponding elements that related to the number of individuals) should be at least 3";
    /**
     * validate number of property.
     */
    String MES_003 = "There should be at least one property";
    /**
     * validate number of .
     */
    String MES_004 = "It should be greater than 0";
    /**
     * validate number of .
     */
    String MES_005 = "Must be greater than 1";
    /**
     * validate array individual size
     */
    String INVALID_ARR_SIZE = "The array's length doesn't match the number of individuals";
    /**
     * validate problem name
     */
    String PROBLEM_NAME = "The problemName should only has 255 characters";
    /**
     * validate population size
     */
    String POPULATION_SIZE = "The populationSize should be less than 1000";
    /**
     * validate generation
     */
    String GENERATION = "The generation value should be less than 100";
    /**
     * validate blank input
     */
    String NOT_BLANK = "must not be empty";
    /**
     * validate evaluate function
     */
    String EVAL_FN_NUM = "The number of evaluateFunctions should be at least 2";
  }

  /**
   * Error code (not message).
   */
  public interface ErrCode {
    /**
     * NOT_EMPTY error
     */
    String NOT_EMPTY = "NotEmpty";
    /**
     * MIN_VALUE error
     */
    String MIN_VALUE = "MinValue";
    /**
     * MAX_VALUE error
     */
    String MAX_VALUE = "MaxValue";
    /**
     * INVALID_FUNCTION error
     */
    String INVALID_FUNCTION = "InvalidFunction";
    /**
     * INVALID_SIZE error
     */
    String INVALID_SIZE = "InvalidSize";
    /**
     * INVALID_LENGTH error
     */
    String INVALID_LENGTH = "InvalidLength";
  }

}