package org.fit.ssapp.constants;

public class MessageConst {

  public interface ErrMessage {

    /**
     * validate number of set
     */
    String MES_001 = "Number of set must be greater or equal to 2";
    /**
     * validate number of individualNumber
     */
    String MES_002 = "The number of individuals (or corresponding elements that related to the number of individuals) should be at least 3";
    String MES_003 = "There should be at least one property";
    String MES_004 = "It should be greater than 0";
    String MES_005 = "Must be greater than 1";
    String MES_006 = "";
    String MES_007 = "";
    String MES_008 = "";
    String INVALID_ARR_SIZE = "The array's length doesn't match the number of individuals";
    String PROBLEM_NAME = "The problemName should only has 255 characters";
    String POPULATION_SIZE = "The populationSize should be less than 1000";
    String GENERATION = "The generation value should be less than 100";
    String NOT_BLANK = "must not be empty";
    String EVAL_FN_NUM = "The number of evaluateFunctions should be at least 2";
  }

  public interface ErrCode {

    String NOT_EMPTY = "NotEmpty";
    String MIN_VALUE = "MinValue";
    String MAX_VALUE = "MaxValue";
    String INVALID_FUNCTION = "InvalidFunction";
    String INVALID_SIZE = "InvalidSize";
    String INVALID_LENGTH = "InvalidLength";
  }

}