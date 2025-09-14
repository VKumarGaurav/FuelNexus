package com.fuel.nexus.service.services;

import com.fuel.nexus.entity.Delivery;
import com.fuel.nexus.dto.DeliveryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Tag(
        name = "Delivery Service",
        description = "Service interface for managing deliveries, delivery agents, and vehicles"
)
public interface DeliveryService {

    // Create a new delivery request
    @Operation(summary = "Create Delivery", description = "Register a new delivery request for gas cylinders or liquid fuel")
    Delivery createDelivery(DeliveryDTO deliveryDTO);

    // Fetch all deliveries with pagination
    @Operation(summary = "Get All Deliveries (Paged)", description = "Fetch all deliveries with pagination support")
    Page<Delivery> getAllDeliveries(Pageable pageable);

    // Get delivery details by ID
    @Operation(summary = "Get Delivery by ID", description = "Retrieve delivery details using delivery ID")
    Optional<Delivery> getDeliveryById(Long deliveryId);

    // Update delivery status (PENDING → DISPATCHED → DELIVERED → CANCELLED)
    @Operation(summary = "Update Delivery Status", description = "Update delivery status for a given delivery ID")
    Delivery updateDeliveryStatus(Long deliveryId, String status);

    // Assign a delivery agent and vehicle
    @Operation(summary = "Assign Agent & Vehicle", description = "Assign a delivery agent and vehicle to a delivery")
    Delivery assignAgentAndVehicle(Long deliveryId, Long agentId, Long vehicleId);

    // Cancel a delivery
    @Operation(summary = "Cancel Delivery", description = "Cancel an ongoing or pending delivery")
    void cancelDelivery(Long deliveryId);

    // Track delivery progress
    @Operation(summary = "Track Delivery", description = "Track the current status of a delivery")
    String trackDelivery(Long deliveryId);
}
