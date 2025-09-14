package com.fuel.nexus.exception.exceptions;

public class NotAuthorizedException extends FuelNexusRuntimeException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}