package com.microvolunteer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    
    private Map<String, String> errors = new HashMap<>();
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String field, String message) {
        super(message);
        this.errors.put(field, message);
    }
    
    public ValidationException(Map<String, String> errors) {
        super("Помилка валідації");
        this.errors = errors;
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
}
