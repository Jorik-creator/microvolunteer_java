package com.microvolunteer.exception;

/**
 * Base exception for business logic errors.
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // Specific business exceptions as static factory methods
    public static BusinessException userNotFound(String identifier) {
        return new BusinessException(
            "User not found: " + identifier,
            "USER_NOT_FOUND"
        );
    }
    
    public static BusinessException taskNotFound(Long taskId) {
        return new BusinessException(
            "Task not found with id: " + taskId,
            "TASK_NOT_FOUND"
        );
    }
    
    public static BusinessException categoryNotFound(Long categoryId) {
        return new BusinessException(
            "Category not found with id: " + categoryId,
            "CATEGORY_NOT_FOUND"
        );
    }
    
    public static BusinessException userAlreadyExists(String email) {
        return new BusinessException(
            "User already exists with email: " + email,
            "USER_ALREADY_EXISTS"
        );
    }
    
    public static BusinessException unauthorizedAccess(String operation) {
        return new BusinessException(
            "Unauthorized access to operation: " + operation,
            "UNAUTHORIZED_ACCESS"
        );
    }
    
    public static BusinessException invalidTaskStatus(String currentStatus, String targetStatus) {
        return new BusinessException(
            "Cannot change task status from " + currentStatus + " to " + targetStatus,
            "INVALID_STATUS_TRANSITION"
        );
    }
    
    public static BusinessException alreadyParticipating(Long taskId) {
        return new BusinessException(
            "User is already participating in task: " + taskId,
            "ALREADY_PARTICIPATING"
        );
    }
    
    public static BusinessException cannotParticipateInOwnTask() {
        return new BusinessException(
            "Task author cannot participate in their own task",
            "CANNOT_PARTICIPATE_OWN_TASK"
        );
    }
    
    public static BusinessException taskNotOpen(Long taskId) {
        return new BusinessException(
            "Task is not open for participation: " + taskId,
            "TASK_NOT_OPEN"
        );
    }
    
    public static BusinessException categoryAlreadyExists(String name) {
        return new BusinessException(
            "Category already exists with name: " + name,
            "CATEGORY_ALREADY_EXISTS"
        );
    }
}
