package com.fuel.nexus.exception.exceptions;

/**
 * Custom exception thrown when a delivery
 * with the given ID or reference is not found.
 */
public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(String message) {
        super(message);
    }

    public DeliveryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

