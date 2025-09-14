package com.fuel.nexus.utility;

/**
 * Enum representing the status of a delivery in the system.
 * Used for tracking the lifecycle of a delivery request.
 */
public enum DeliveryStatus {

    PENDING("Delivery request created, awaiting dispatch"),
    DISPATCHED("Delivery has been dispatched and is in transit"),
    DELIVERED("Delivery successfully completed"),
    CANCELLED("Delivery cancelled due to request or issue");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    /**
     * Get a human-readable description of the delivery status.
     *
     * @return description of the status
     */
    public String getDescription() {
        return description;
    }
}
