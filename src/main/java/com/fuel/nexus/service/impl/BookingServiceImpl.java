package com.fuel.nexus.service.impl;

import com.fuel.nexus.entity.Booking;
import com.fuel.nexus.entity.FuelInventory;
import com.fuel.nexus.exception.exceptions.BookingNotFoundException;
import com.fuel.nexus.repository.BookingRepository;
import com.fuel.nexus.repository.FuelInventoryRepository;
import com.fuel.nexus.service.services.BookingService;
import com.fuel.nexus.utility.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Booking & Order Management Service", description = "Implementation of booking management, order handling, and inventory updates")
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FuelInventoryRepository inventoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_BOOKING = "booking-events";

    // ------------------------------------------------------------------------
    // Create Booking
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @Operation(
            summary = "Create Booking",
            description = "Creates a new booking for gas cylinders or liquid fuel. Publishes event to Kafka and caches result."
    )
    public Booking createBooking(Booking booking) {
        log.info("Creating new booking for customer: {}", booking.getCustomer());

        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);

        // Publish event to Kafka
        kafkaTemplate.send(TOPIC_BOOKING, "Booking created with ID: " + savedBooking.getId());
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        return savedBooking;
    }

    // ------------------------------------------------------------------------
    // Update Booking Status
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "bookings", key = "#bookingId") // Clear cache on status change
    @Operation(
            summary = "Update Booking Status",
            description = "Updates the booking status (PENDING, APPROVED, DELIVERED). Publishes event to Kafka."
    )
    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        log.info("Updating booking status for ID: {} to {}", bookingId, status);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        kafkaTemplate.send(TOPIC_BOOKING, "Booking status updated: " + bookingId + " -> " + status);
        log.info("Booking ID: {} updated to status: {}", bookingId, status);

        return updatedBooking;
    }

    @Override
    public Page<Booking> getAllBookings(Pageable pageable) {
        log.info("Fetching all bookings with pagination, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return bookingRepository.findAll(pageable);
    }


    // ------------------------------------------------------------------------
    // Update Inventory on Delivery
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "bookings", key = "#bookingId") // Evict cache on delivery
    @Operation(
            summary = "Update Inventory on Delivery",
            description = "Automatically updates inventory when a booking is marked as DELIVERED."
    )
    public void updateInventoryOnDelivery(Long bookingId) {
        log.info("Processing inventory update for delivered booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.DELIVERED) {
            throw new IllegalStateException("Booking must be DELIVERED to update inventory");
        }

        FuelInventory inventory = (FuelInventory) inventoryRepository.findByFuelType(booking.getFuelType())
                .orElseThrow(() -> new IllegalStateException("No inventory found for fuel type: " + booking.getFuelType()));

        if (inventory.getAvailableQuantity() < booking.getQuantity()) {
            throw new IllegalStateException("Insufficient inventory for delivery");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - booking.getQuantity());
        inventoryRepository.save(inventory);

        kafkaTemplate.send(TOPIC_BOOKING, "Inventory updated for booking ID: " + bookingId);
        log.info("Inventory successfully updated for booking ID: {}", bookingId);
    }

    // ------------------------------------------------------------------------
    // Fetch Booking with Caching
    // ------------------------------------------------------------------------
    @Cacheable(value = "bookings", key = "#bookingId")
    @Operation(
            summary = "Get Booking by ID",
            description = "Fetch booking details by ID. Uses cache for performance optimization."
    )
    public Booking getBookingById(Long bookingId) {
        log.info("Fetching booking with ID: {}", bookingId);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));
    }
}
