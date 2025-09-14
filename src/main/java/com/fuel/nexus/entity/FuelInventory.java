package com.fuel.nexus.entity;

import com.fuel.nexus.utility.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fuel_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FuelInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product cannot be null")
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Available quantity cannot be null")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Double availableQuantity;

    @NotNull(message = "Storage location cannot be null")
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

