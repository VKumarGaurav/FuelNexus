package com.fuel.nexus.exception.exceptions;

public class ResourceNotFoundException extends FuelNexusRuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}