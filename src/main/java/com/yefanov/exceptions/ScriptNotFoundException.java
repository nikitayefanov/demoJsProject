package com.yefanov.exceptions;

/**
 * Thrown to indicate that script with such id doesn't exist
 */
public class ScriptNotFoundException extends RuntimeException {

    public ScriptNotFoundException(String message) {
        super(message);
    }

    public ScriptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptNotFoundException(Throwable cause) {
        super(cause);
    }

    public ScriptNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ScriptNotFoundException() {
    }
}
