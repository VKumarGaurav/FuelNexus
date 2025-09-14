package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.DeliveryDTO;
import com.fuel.nexus.entity.Delivery;
import com.fuel.nexus.exception.exceptions.DeliveryNotFoundException;
import com.fuel.nexus.repository.DeliveryRepository;
import com.fuel.nexus.service.services.DeliveryService;
import com.fuel.nexus.utility.DeliveryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(
        name = "Delivery Service Implementation",
        description = "Handles creation, status updates, assignment of agents/vehicles, cancellation, and tracking of deliveries"
)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ModelMapper modelMapper;

    private static final String TOPIC_DELIVERY = "delivery-events";

    // ------------------------------------------------------------------------
    // Create a new delivery request
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @Operation(summary = "Create Delivery", description = "Registers a new delivery request")
    public Delivery createDelivery(DeliveryDTO deliveryDTO) {
        log.info("Creating new delivery for customer: {}", deliveryDTO.getCustomerName());

        Delivery delivery = modelMapper.map(deliveryDTO, Delivery.class);
        delivery.setDeliveryStatus(DeliveryStatus.PENDING);

        Delivery savedDelivery = deliveryRepository.save(delivery);
        kafkaTemplate.send(TOPIC_DELIVERY, "New delivery created with ID: " + savedDelivery.getId());

        log.info("Delivery created successfully with ID: {}", savedDelivery.getId());
        return savedDelivery;
    }

    // ------------------------------------------------------------------------
    // Fetch all deliveries with pagination
    // ------------------------------------------------------------------------
    @Override
    @Cacheable(value = "deliveries", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Operation(summary = "Get All Deliveries (Paged)", description = "Fetch all deliveries with pagination support")
    public Page<Delivery> getAllDeliveries(Pageable pageable) {
        log.info("Fetching deliveries page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return deliveryRepository.findAll(pageable);
    }

    // ------------------------------------------------------------------------
    // Get delivery by ID
    // ------------------------------------------------------------------------
    @Override
    @Cacheable(value = "deliveries", key = "#deliveryId")
    @Operation(summary = "Get Delivery by ID", description = "Retrieve delivery details using delivery ID")
    public Optional<Delivery> getDeliveryById(Long deliveryId) {
        log.info("Fetching delivery with ID: {}", deliveryId);
        return Optional.ofNullable(
                deliveryRepository.findById(deliveryId)
                        .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + deliveryId))
        );
    }

    // ------------------------------------------------------------------------
    // Update delivery status
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "deliveries", key = "#deliveryId")
    @Operation(summary = "Update Delivery Status", description = "Update delivery status for a given delivery ID")
    public Delivery updateDeliveryStatus(Long deliveryId, String status) {
        log.info("Updating status for delivery ID: {} -> {}", deliveryId, status);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + deliveryId));

        DeliveryStatus newStatus;
        try {
            newStatus = DeliveryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid delivery status: " + status);
        }

        delivery.setDeliveryStatus(newStatus);
        Delivery updated = deliveryRepository.save(delivery);

        kafkaTemplate.send(TOPIC_DELIVERY, "Delivery ID " + deliveryId + " updated to " + status);
        log.info("Delivery ID: {} updated to {}", deliveryId, status);

        return updated;
    }

    // ------------------------------------------------------------------------
    // Assign delivery agent and vehicle
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "deliveries", key = "#deliveryId")
    @Operation(summary = "Assign Agent & Vehicle", description = "Assign a delivery agent and vehicle to a delivery")
    public Delivery assignAgentAndVehicle(Long deliveryId, Long agentId, Long vehicleId) {
        log.info("Assigning agent {} and vehicle {} to delivery ID: {}", agentId, vehicleId, deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + deliveryId));

        delivery.setAgentId(agentId);
        delivery.setVehicleId(vehicleId);

        Delivery updated = deliveryRepository.save(delivery);

        kafkaTemplate.send(TOPIC_DELIVERY, "Agent " + agentId + " and Vehicle " + vehicleId +
                " assigned to Delivery ID: " + deliveryId);
        log.info("Agent {} and Vehicle {} assigned to Delivery ID: {}", agentId, vehicleId, deliveryId);

        return updated;
    }

    // ------------------------------------------------------------------------
    // Cancel delivery
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "deliveries", key = "#deliveryId")
    @Operation(summary = "Cancel Delivery", description = "Cancel an ongoing or pending delivery")
    public void cancelDelivery(Long deliveryId) {
        log.info("Cancelling delivery ID: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + deliveryId));

        if (delivery.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }

        delivery.setDeliveryStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);

        kafkaTemplate.send(TOPIC_DELIVERY, "Delivery ID " + deliveryId + " cancelled");
        log.info("Delivery ID: {} successfully cancelled", deliveryId);
    }

    // ------------------------------------------------------------------------
    // Track delivery
    // ------------------------------------------------------------------------
    @Override
    @Cacheable(value = "deliveries", key = "#deliveryId")
    @Operation(summary = "Track Delivery", description = "Track the current status of a delivery")
    public String trackDelivery(Long deliveryId) {
        log.info("Tracking delivery with ID: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with ID: " + deliveryId));

        log.info("Current status of delivery ID {}: {}", deliveryId, delivery.getDeliveryStatus());
        return "Delivery ID " + deliveryId + " is currently " + delivery.getDeliveryStatus();
    }
}
