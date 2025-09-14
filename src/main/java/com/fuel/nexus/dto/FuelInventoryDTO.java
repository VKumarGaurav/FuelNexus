package com.fuel.nexus.dto;

import com.fuel.nexus.utility.FuelType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FuelInventoryDTO {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Available quantity cannot be null")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Double availableQuantity;

    @NotBlank(message = "Storage location is required")
    @Size(min = 3, max = 100, message = "Storage location must be between 3 and 100 characters")
    private String storageLocation;

    @PastOrPresent(message = "Last updated date cannot be in the future")
    private LocalDateTime lastUpdated;

    @NotBlank(message = "Batch number is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Batch number must contain only uppercase letters, digits, or hyphens")
    private String batchNumber;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;
}

