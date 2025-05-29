package com.microvolunteer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s з ID %d не знайдено", resource, id));
    }
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s '%s' не знайдено", resource, identifier));
    }
}
