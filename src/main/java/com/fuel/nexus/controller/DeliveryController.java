package com.fuel.nexus.controller;

import com.fuel.nexus.dto.DeliveryDTO;
import com.fuel.nexus.entity.Delivery;
import com.fuel.nexus.exception.exceptions.DeliveryNotFoundException;
import com.fuel.nexus.service.services.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries")
@Tag(
        name = "Delivery Controller",
        description = "REST API for managing deliveries with caching, Kafka notifications, and exception handling"
)
public class DeliveryController {

    private final DeliveryService deliveryService;

    // ------------------------------------------------------------------------
    // Create a new delivery
    // ------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Create Delivery", description = "Registers a new delivery request",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Delivery created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Delivery.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Delivery> createDelivery(@RequestBody DeliveryDTO deliveryDTO) {
        log.info("API Request: Create Delivery for customer {}", deliveryDTO.getCustomerName());
        Delivery saved = deliveryService.createDelivery(deliveryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ------------------------------------------------------------------------
    // Get all deliveries with pagination
    // ------------------------------------------------------------------------
    @GetMapping
    @Cacheable(value = "deliveries", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Operation(summary = "Get All Deliveries (Paged)", description = "Fetch all deliveries with pagination support",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fetched deliveries successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Page<Delivery>> getAllDeliveries(Pageable pageable) {
        log.info("API Request: Fetching all deliveries with pagination");
        return ResponseEntity.ok(deliveryService.getAllDeliveries(pageable));
    }

    // ------------------------------------------------------------------------
    // Get delivery by ID
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Cacheable(value = "deliveries", key = "#id")
    @Operation(summary = "Get Delivery by ID", description = "Retrieve delivery details using delivery ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Delivery.class))),
                    @ApiResponse(responseCode = "404", description = "Delivery not found",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long id) {
        log.info("API Request: Fetch Delivery by ID {}", id);
        return ResponseEntity.ok(
                deliveryService.getDeliveryById(id)
                        .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + id))
        );
    }

    // ------------------------------------------------------------------------
    // Update delivery status
    // ------------------------------------------------------------------------
    @PatchMapping("/{id}/status")
    @CacheEvict(value = "deliveries", key = "#id")
    @Operation(summary = "Update Delivery Status", description = "Update delivery status for a given delivery ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery status updated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Delivery.class))),
                    @ApiResponse(responseCode = "404", description = "Delivery not found",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Delivery> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("API Request: Update Delivery ID {} status -> {}", id, status);
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status));
    }

    // ------------------------------------------------------------------------
    // Assign agent & vehicle
    // ------------------------------------------------------------------------
    @PatchMapping("/{id}/assign")
    @CacheEvict(value = "deliveries", key = "#id")
    @Operation(summary = "Assign Agent & Vehicle", description = "Assign a delivery agent and vehicle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agent & Vehicle assigned",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Delivery.class))),
                    @ApiResponse(responseCode = "404", description = "Delivery not found",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Delivery> assignAgentAndVehicle(
            @PathVariable Long id,
            @RequestParam Long agentId,
            @RequestParam Long vehicleId
    ) {
        log.info("API Request: Assign Agent {} and Vehicle {} to Delivery {}", agentId, vehicleId, id);
        return ResponseEntity.ok(deliveryService.assignAgentAndVehicle(id, agentId, vehicleId));
    }

    // ------------------------------------------------------------------------
    // Cancel delivery
    // ------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @CacheEvict(value = "deliveries", key = "#id")
    @Operation(summary = "Cancel Delivery", description = "Cancel an ongoing or pending delivery",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Delivery cancelled"),
                    @ApiResponse(responseCode = "404", description = "Delivery not found",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Void> cancelDelivery(@PathVariable Long id) {
        log.info("API Request: Cancel Delivery {}", id);
        deliveryService.cancelDelivery(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------
    // Track delivery
    // ------------------------------------------------------------------------
    @GetMapping("/{id}/track")
    @Cacheable(value = "deliveries", key = "#id")
    @Operation(summary = "Track Delivery", description = "Track current status of a delivery",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery tracked successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Delivery not found",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<String> trackDelivery(@PathVariable Long id) {
        log.info("API Request: Track Delivery {}", id);
        return ResponseEntity.ok(deliveryService.trackDelivery(id));
    }
}

