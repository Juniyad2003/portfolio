package com.finance.exception;

public class InvalidTransactionIdException extends Exception {

    public InvalidTransactionIdException() {
    }

    public InvalidTransactionIdException(String message) {
        super(message);
    }

    public InvalidTransactionIdException(Throwable cause) {
        super(cause);
    }
}
