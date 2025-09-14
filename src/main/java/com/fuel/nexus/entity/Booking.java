package com.fuel.nexus.entity;

import com.fuel.nexus.utility.BookingStatus;
import com.fuel.nexus.utility.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Product must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Booking quantity cannot be null")
    @Min(value = 1, message = "Minimum booking quantity is 1")
    private Double quantity;

    @NotNull(message = "Booking date cannot be null")
    @PastOrPresent(message = "Booking date cannot be in the future")
    private LocalDateTime bookingDate;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "PENDING|CONFIRMED|CANCELLED", message = "Invalid booking status")
    private BookingStatus status;

    @NotBlank(message = "Type  cannot be blank")
    private FuelType fuelType;
}
