package org.fit.ssapp.util;

import org.fit.ssapp.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {

  /**
   * return a code 500 response with message
   *
   * @param  message String
   * @return ResponseEntity<Response>
   */
  public static ResponseEntity<Response> getInternalErrorResponse(String message) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Response
            .builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message(message)
            .data(null)
            .build());
  }

  /**
   * return a code 500 response with message and data
   *
   * @param  message String
   * @return ResponseEntity<Response>
   */
  public static ResponseEntity<Response> getInternalErrorResponse(String message, Object data) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Response
            .builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message(message)
            .data(data)
            .build());
  }
}
