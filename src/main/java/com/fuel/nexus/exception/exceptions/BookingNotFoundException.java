package com.fuel.nexus.exception.exceptions;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String id) {
        super("Booking not found with ID: " + id);
    }
}
