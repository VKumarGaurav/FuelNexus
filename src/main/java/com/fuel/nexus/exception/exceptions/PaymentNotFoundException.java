package com.fuel.nexus.exception.exceptions;

/**
 * Custom exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
