package com.fuel.nexus.service.services;

import com.fuel.nexus.entity.Booking;
import com.fuel.nexus.utility.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Booking & Order Management Service
 * Handles booking of gas cylinders and liquid fuel,
 * manages booking lifecycle and updates inventory on delivery.
 */
@Tag(name = "Booking & Order Management", description = "Service for managing fuel bookings and orders")
public interface BookingService {

    /**
     * Allow customers to book gas cylinders or liquid fuel
     */
    @Operation(summary = "Create Booking", description = "Allow customers to create a new booking for gas cylinders or liquid fuel.")
    Booking createBooking(Booking booking);

    /**
     * Update booking status: pending, approved, delivered, cancelled
     */
    @Operation(summary = "Update Booking Status", description = "Update the booking status for a specific booking (e.g., PENDING, APPROVED, DELIVERED, CANCELLED).")
    Booking updateBookingStatus(Long bookingId, BookingStatus status);

    /**
     * Get all bookings with pagination
     */
    @Operation(summary = "Get All Bookings", description = "Fetch all bookings with pagination support.")
    Page<Booking> getAllBookings(Pageable pageable);

    /**
     * Fetch booking details by booking ID
     */
    @Operation(summary = "Get Booking By ID", description = "Fetch booking details using booking ID.")
    Booking getBookingById(Long bookingId);

    /**
     * Auto-update inventory when delivery is marked as completed
     */
    @Operation(summary = "Update Inventory on Delivery", description = "Automatically update fuel inventory after successful delivery of booking.")
    void updateInventoryOnDelivery(Long bookingId);

}
