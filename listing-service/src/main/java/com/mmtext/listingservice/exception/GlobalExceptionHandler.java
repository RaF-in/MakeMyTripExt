package com.mmtext.listingservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler  {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
   public ResponseEntity<String> handleException(Exception e) {
       return  ResponseEntity.internalServerError().body(e.getMessage());
   }
   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
       Map<String, String> errors = new HashMap<>();
       e.getBindingResult().getAllErrors().forEach((error) -> {
           errors.put(error.getObjectName(), error.getDefaultMessage());
       });
       return ResponseEntity.badRequest().body(errors);
   }
    @ExceptionHandler(AirCraftAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleAirCraftAlreadyExistException(Exception e) {
       return  ResponseEntity.badRequest().body(getErrors("message", e));
    }
    @ExceptionHandler(BusAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleBusAlreadyExistException(Exception e) {
        return  ResponseEntity.badRequest().body(getErrors(null, e));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return  ResponseEntity.badRequest().body(getErrors("message", e));
    }
    private Map<String, String> getErrors(String key, Exception e) {
        if (key == null) {
            key = "message";
        }
        log.error(e.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put(key, e.getMessage());
        return errors;
    }
}
