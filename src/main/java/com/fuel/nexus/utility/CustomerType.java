package com.fuel.nexus.utility;

public enum CustomerType {
    DOMESTIC, COMMERCIAL, INDUSTRIAL;
    /**
     * Utility method to check if a string is a valid CustomerType
     * @param type string representation
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String type) {
        for (CustomerType ct : CustomerType.values()) {
            if (ct.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}

