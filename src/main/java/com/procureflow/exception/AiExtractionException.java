package com.procureflow.exception;

public class AiExtractionException
        extends RuntimeException {

    public AiExtractionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}