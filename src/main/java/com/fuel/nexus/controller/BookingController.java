package com.fuel.nexus.controller;

import com.fuel.nexus.entity.Booking;
import com.fuel.nexus.service.services.BookingService;
import com.fuel.nexus.utility.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Booking & Order Management
 * Handles booking creation, status updates, inventory updates, and retrieval
 * Includes Kafka event publishing and cache integration for performance
 */
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking & Order Management Controller", description = "APIs for managing bookings, orders, and fuel inventory updates")
public class BookingController {

    private final BookingService bookingService;

    // ------------------------------------------------------------------------
    // Create Booking
    // ------------------------------------------------------------------------
    @PostMapping("/create")
    @Operation(
            summary = "Create Booking",
            description = "Allows customers to book gas cylinders or liquid fuel. " +
                    "Booking status defaults to PENDING. Publishes event to Kafka.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Booking created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid booking request", content = @Content)
            }
    )
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        log.info("Received request to create booking for customer={}", booking.getCustomer());
        Booking createdBooking = bookingService.createBooking(booking);
        log.info("Booking created successfully with ID={}", createdBooking.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    // ------------------------------------------------------------------------
    // Update Booking Status
    // ------------------------------------------------------------------------
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update Booking Status",
            description = "Updates the status of a booking (PENDING, APPROVED, DELIVERED). Publishes event to Kafka.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))),
                    @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
            }
    )
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        log.info("Received request to update booking ID={} to status={}", id, status);
        Booking updatedBooking = bookingService.updateBookingStatus(id, status);
        log.info("Booking ID={} updated successfully to status={}", id, status);
        return ResponseEntity.ok(updatedBooking);
    }

    // ------------------------------------------------------------------------
    // Get Booking by ID (Cache-enabled)
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(
            summary = "Get Booking by ID",
            description = "Fetches booking details by ID. Uses cache for performance.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))),
                    @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
            }
    )
    @Cacheable(value = "bookings", key = "#id")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking by ID={}", id);
        Booking booking = bookingService.getBookingById(id);
        log.info("Booking retrieved successfully with ID={}", id);
        return ResponseEntity.ok(booking);
    }

    // ------------------------------------------------------------------------
    // Get All Bookings (Paged)
    // ------------------------------------------------------------------------
    @GetMapping("/all")
    @Operation(
            summary = "Get All Bookings (Paged)",
            description = "Fetches all bookings with pagination support.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<Page<Booking>> getAllBookings(Pageable pageable) {
        log.info("Fetching all bookings, page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Booking> bookings = bookingService.getAllBookings(pageable);
        log.info("Retrieved {} bookings", bookings.getTotalElements());
        return ResponseEntity.ok(bookings);
    }

    // ------------------------------------------------------------------------
    // Update Inventory on Delivery
    // ------------------------------------------------------------------------
    @PutMapping("/{id}/deliver")
    @Operation(
            summary = "Update Inventory on Delivery",
            description = "Automatically updates inventory when a booking is marked as DELIVERED. Publishes event to Kafka.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Booking is not marked as DELIVERED", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
            }
    )
    @CacheEvict(value = "bookings", key = "#id")
    public ResponseEntity<String> updateInventoryOnDelivery(@PathVariable Long id) {
        log.info("Received request to update inventory for delivered booking ID={}", id);
        bookingService.updateInventoryOnDelivery(id);
        log.info("Inventory updated successfully for booking ID={}", id);
        return ResponseEntity.ok("Inventory updated successfully for booking ID: " + id);
    }
}
