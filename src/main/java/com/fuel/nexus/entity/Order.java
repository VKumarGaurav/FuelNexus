package com.fuel.nexus.entity;

import com.fuel.nexus.utility.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Booking must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull(message = "Order date cannot be null")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDateTime orderDate;

    @NotBlank(message = "Order status is required")
    @Pattern(regexp = "PENDING|DISPATCHED|DELIVERED|CANCELLED", message = "Invalid order status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.1", message = "Total amount must be greater than 0")
    private Double totalAmount;
}
