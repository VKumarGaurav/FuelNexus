package com.fuel.nexus.utility;

public enum OrderStatus {
    PENDING,        // Order placed but not yet processed
    DISPATCHED,     // Order has been sent out for delivery
    DELIVERED,      // Order successfully delivered to customer
    CANCELLED       // Order was cancelled by customer or system
}
