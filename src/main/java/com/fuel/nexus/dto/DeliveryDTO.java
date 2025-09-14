package com.fuel.nexus.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(min = 2, max = 50, message = "Customer name must be between 2 and 50 characters")
    private String customerName;

    @NotBlank(message = "Customer email cannot be blank")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer contact number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit number")
    private String customerContact;

    @NotBlank(message = "Delivery address cannot be blank")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String deliveryAddress;

    @NotBlank(message = "Fuel type is required")
    @Pattern(regexp = "GAS|LIQUID", message = "Fuel type must be GAS or LIQUID")
    private String fuelType;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1 unit")
    private Integer quantity;

    @PastOrPresent(message = "Requested date cannot be in the future")
    private LocalDateTime requestedDate;

    @NotBlank(message = "Delivery status cannot be blank")
    @Pattern(
            regexp = "PENDING|DISPATCHED|DELIVERED|CANCELLED",
            message = "Invalid delivery status"
    )
    private String status;

    @NotNull(message = "Agent ID cannot be null")
    private Long agentId;

    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;
}
