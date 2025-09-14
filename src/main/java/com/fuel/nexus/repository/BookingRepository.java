package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Booking;
import com.fuel.nexus.utility.BookingStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Booking entity.
 * Provides CRUD operations and custom queries for booking management.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    
    // Find bookings by customer name
    List<Booking> findByCustomerName(String customerName);

    
    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    
    // Find bookings by fuel type
    List<Booking> findByFuelType(String fuelType);
}
