package org.fit.ssapp.exception;

import lombok.Getter;

@Getter
public class ZeroWeightSum extends RuntimeException {

  private final int individualIndex;

  public ZeroWeightSum(int individualIndex) {
    super(getErrorMessage(individualIndex));
    this.individualIndex = individualIndex;
  }

  private static String getErrorMessage(int individualIndex) {
    return String.format("Encountered zero-sum of weights for the individual: %d"
            + ", please re-check the dataset", individualIndex);
  }
}
