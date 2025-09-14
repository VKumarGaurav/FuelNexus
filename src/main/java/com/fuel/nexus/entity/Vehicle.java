package com.fuel.nexus.entity;

import com.fuel.nexus.utility.VehicleStatus;
import com.fuel.nexus.utility.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vehicle number cannot be blank")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$",
            message = "Invalid vehicle number (Format: XX00XX0000)")
    private String vehicleNumber;

    @NotBlank(message = "Vehicle type cannot be blank")
    @Pattern(regexp = "TRUCK|TANKER|VAN", message = "Vehicle type must be TRUCK, TANKER, or VAN")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 100, message = "Capacity must be at least 100 liters")
    private Double capacity;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "AVAILABLE|IN_USE|MAINTENANCE",
            message = "Invalid vehicle status")
    @Enumerated(EnumType.STRING)
    private VehicleStatus vehicleStatus;
}
