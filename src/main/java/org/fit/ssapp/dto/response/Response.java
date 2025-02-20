package org.fit.ssapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents a *standard API response* structure for service requests.
 * This class is used to encapsulate the **status**, **message**, and **data** returned
 * by the server when handling API requests.
 * ## **Main Attributes:**
 * - **`status`** → The HTTP status code of the response (e.g., `200`, `500`).
 * - **`message`** → A descriptive message about the response result.
 * - **`data`** → The actual response payload, which can be any type of object.
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
