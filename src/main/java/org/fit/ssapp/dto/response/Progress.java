package org.fit.ssapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents the progress status of an ongoing computation or task.
 * This class is used to track and communicate the **execution progress** of algorithms
 * solving the Stable Matching Problem
 * Status message (`message`) → Describes the current stage of execution.
 * Progress flag (`inProgress`) → Indicates whether the computation is still running.
 * Estimated time left (`minuteLeft`) → Provides an estimate of the remaining execution time.
 * Elapsed runtime (`runtime`) → The total execution time (in seconds).
 * Completion percentage (`percentage`) → Represents how much of the task is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress {

  private String message;
  private boolean inProgress;
  private int minuteLeft;
  private Double runtime;
  private Integer percentage;

  /**
   * getMinuteLeft.
   *
   * @return int
   */
  public int getMinuteLeft() {

    if (minuteLeft == 0) {
      return 1; // return 1 if the minuteLeft is less than 60 seconds
    }

    return minuteLeft;
  }
}
