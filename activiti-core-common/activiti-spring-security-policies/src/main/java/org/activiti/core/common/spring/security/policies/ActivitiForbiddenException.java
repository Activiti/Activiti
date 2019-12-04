package org.activiti.core.common.spring.security.policies;

public class ActivitiForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ActivitiForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActivitiForbiddenException(String message) {
        super(message);
    }
}