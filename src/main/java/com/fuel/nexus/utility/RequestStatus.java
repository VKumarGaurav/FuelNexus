package com.fuel.nexus.utility;

public enum RequestStatus {
    PENDING, IN_PROGRESS, RESOLVED, REJECTED;

    /**
     * Utility method to get enum from string (case-insensitive)
     * @param status string value
     * @return RequestStatus enum
     * @throws IllegalArgumentException if invalid
     */
    public static RequestStatus fromString(String status) {
        for (RequestStatus rs : RequestStatus.values()) {
            if (rs.name().equalsIgnoreCase(status)) {
                return rs;
            }
        }
        throw new IllegalArgumentException("Invalid RequestStatus: " + status);
    }
}

