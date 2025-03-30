package org.fit.ssapp.dto.request;

import org.fit.ssapp.constants.MessageConst.ErrMessage;
import org.fit.ssapp.dto.validator.ValidDistributedCores;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base request dto class for this application.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class ProblemDto {

  /**
   * Get code name of one of MOEA supported Genetic Algorithms.
   *
   * @return algorithm code name.
   */
  @NotNull(message = ErrMessage.NOT_BLANK)
  @NotEmpty(message = ErrMessage.NOT_BLANK)
  protected String algorithm;

  @ValidDistributedCores
  protected String distributedCores;

  @NotNull(message = ErrMessage.NOT_BLANK)
  protected Integer maxTime;

  @NotNull(message = ErrMessage.NOT_BLANK)
  protected Integer generation;

  @NotNull(message = ErrMessage.NOT_BLANK)
  protected Integer populationSize;

  @NotNull(message = ErrMessage.NOT_BLANK)
  @NotEmpty(message = ErrMessage.NOT_BLANK)
  @Size(max = 255, message = ErrMessage.PROBLEM_NAME)
  protected String problemName;
}
