package org.fit.ssapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  public int getMinuteLeft() {

    if (minuteLeft == 0) {
      return 1; // return 1 if the minuteLeft is less than 60 seconds
    }

    return minuteLeft;
  }
}
