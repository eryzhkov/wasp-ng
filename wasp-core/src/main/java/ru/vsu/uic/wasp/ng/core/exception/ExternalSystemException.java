package ru.vsu.uic.wasp.ng.core.exception;

public class ExternalSystemException extends Exception {

    public ExternalSystemException() {
        super();
    }

    public ExternalSystemException(String message) {
        super(message);
    }

    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalSystemException(Throwable cause) {
        super(cause);
    }

    protected ExternalSystemException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
