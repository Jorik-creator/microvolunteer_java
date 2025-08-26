package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.dto.TaskRequest;

public class TaskDatesValidator implements ConstraintValidator<ValidTaskDates, TaskRequest> {
    
    @Override
    public boolean isValid(TaskRequest taskRequest, ConstraintValidatorContext context) {
        if (taskRequest.getStartDate() == null) {
            return true;
        }
        
        if (taskRequest.getEndDate() == null) {
            return true;
        }
        
        return taskRequest.getEndDate().isAfter(taskRequest.getStartDate());
    }
}