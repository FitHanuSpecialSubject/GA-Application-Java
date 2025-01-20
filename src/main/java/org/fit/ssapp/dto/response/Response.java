package org.fit.ssapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

  private int status;
  private String message;
  private Object data;
}
