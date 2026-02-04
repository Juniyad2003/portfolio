package com.finance.exception;

public class InvalidAssetIdException extends Exception {

    public InvalidAssetIdException() {}

    public InvalidAssetIdException(String message) {
        super(message);
    }

    public InvalidAssetIdException(Throwable cause) {
        super(cause);
    }
}