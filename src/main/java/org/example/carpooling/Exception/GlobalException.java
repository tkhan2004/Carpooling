package org.example.carpooling.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalException extends RuntimeException {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<String> handleException(GlobalException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
