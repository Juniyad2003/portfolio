package com.finance.exception;

public class InvalidPortfolioIdException extends Exception {

    public InvalidPortfolioIdException() {}

    public InvalidPortfolioIdException(String message) {
        super(message);
    }

    public InvalidPortfolioIdException(Throwable cause) {
        super(cause);
    }

    public InvalidPortfolioIdException(String message, Throwable cause) {
        super(message, cause);
    }
}