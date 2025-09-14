package com.fuel.nexus.exception.exceptions;

/**
 * Custom exception thrown when a Billing record is not found.
 */
public class BillingNotFoundException extends RuntimeException {

    public BillingNotFoundException(String message) {
        super(message);
    }

    public BillingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
