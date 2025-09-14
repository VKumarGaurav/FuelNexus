package com.fuel.nexus.utility;

/**
 * Enum representing the current operational status
 * of a delivery vehicle.
 */
public enum VehicleStatus {
    AVAILABLE("Vehicle is available for assignment"),
    IN_USE("Vehicle is currently being used for delivery"),
    MAINTENANCE("Vehicle is under maintenance or repair");

    private final String description;

    VehicleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

