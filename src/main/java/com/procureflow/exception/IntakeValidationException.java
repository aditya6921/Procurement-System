package com.procureflow.exception;

import java.util.List;

public class IntakeValidationException
        extends RuntimeException {

    private final List<String> errors;

    public IntakeValidationException(
            String message,
            List<String> errors
    ) {

        super(message);

        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}