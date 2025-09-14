package com.fuel.nexus.entity;

import com.fuel.nexus.utility.DeliveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId; // Reference to Booking

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId; // Reference to Customer

    @PastOrPresent(message = "Delivery date cannot be in the future")
    private LocalDateTime deliveryDate;

    @NotBlank(message = "Delivery address cannot be blank")
    @Size(min = 10, max = 255, message = "Delivery address must be between 10 and 255 characters")
    private String deliveryAddress;

    @NotBlank(message = "Delivery status cannot be blank")
    @Pattern(regexp = "PENDING|DISPATCHED|DELIVERED|CANCELLED",
            message = "Invalid delivery status")
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private DeliveryAgent assignedAgent;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle assignedVehicle;

    @NotNull(message = "Agent ID cannot be null")
    private Long agentId;

    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;
}





//9019743844
