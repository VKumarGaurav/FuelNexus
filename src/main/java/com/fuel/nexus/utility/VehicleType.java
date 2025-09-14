package com.fuel.nexus.utility;

/**
 * Enum representing different types of vehicles
 * used in fuel delivery management.
 */
public enum VehicleType {
    TRUCK("Truck for bulk transport"),
    TANKER("Tanker for liquid fuel transport"),
    VAN("Van for small or local deliveries");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
