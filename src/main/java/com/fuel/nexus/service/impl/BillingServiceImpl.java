package com.fuel.nexus.service.impl;

import com.fuel.nexus.entity.Billing;
import com.fuel.nexus.exception.exceptions.BillingNotFoundException;
import com.fuel.nexus.repository.BillingRepository;
import com.fuel.nexus.service.services.BillingService;
import com.fuel.nexus.utility.BillingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Billing Service Implementation", description = "Handles business logic for Billing operations")
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BILLING_TOPIC = "billing-events";

    // Create a new billing record
    @Override
    @Operation(summary = "Create billing", description = "Creates and stores a new billing record, then publishes event to Kafka")
    public Billing createBilling(Billing billing) {
        log.info("Creating new billing record for customer: {}", billing.getCustomerEmail());
        Billing savedBilling = billingRepository.save(billing);
        kafkaTemplate.send(BILLING_TOPIC, "BillingCreated", savedBilling);
        log.debug("Billing event published to Kafka for ID: {}", savedBilling.getId());
        return savedBilling;
    }

    // Get billing by ID
    @Override
    @Cacheable(value = "billings", key = "#id")
    @Operation(summary = "Get billing by ID", description = "Retrieves billing record by ID with caching support")
    public Optional<Billing> getBillingById(Long id) {
        log.info("Fetching billing record by ID: {}", id);
        return billingRepository.findById(id);
    }

    // Get all billings (paginated)
    @Override
    @Operation(summary = "Get all billings (paginated)", description = "Returns paginated billing records")
    public Page<Billing> getAllBillings(Pageable pageable) {
        log.info("Fetching all billing records with pagination: {}", pageable);
        return billingRepository.findAll(pageable);
    }

    // Get billings by customer email
    @Override
    @Operation(summary = "Get billings by customer email", description = "Fetch billing records for a customer email")
    public List<Billing> getBillingsByCustomerEmail(String customerEmail) {
        log.info("Fetching billing records for customer email: {}", customerEmail);
        return billingRepository.findByCustomerEmail(customerEmail);
    }

    // Get billings by status
    @Override
    @Operation(summary = "Get billings by status", description = "Fetch billing records filtered by status")
    public List<Billing> getBillingsByStatus(BillingStatus status) {
        log.info("Fetching billing records with status: {}", status);
        return billingRepository.findByStatus(status);
    }

    // Update billing status
    @Override
    @CachePut(value = "billings", key = "#billingId")
    @Operation(summary = "Update billing status", description = "Update the status of a billing record and publish Kafka event")
    public Billing updateBillingStatus(Long billingId, BillingStatus status) {
        log.info("Updating billing status for ID: {} to {}", billingId, status);
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new BillingNotFoundException("Billing record not found with ID: " + billingId));

        billing.setBillingStatus(status);
        Billing updatedBilling = billingRepository.save(billing);

        kafkaTemplate.send(BILLING_TOPIC, "BillingStatusUpdated", updatedBilling);
        log.debug("Kafka event published for billing status update, ID: {}", billingId);

        return updatedBilling;
    }

    // Delete billing
    @Override
    @CacheEvict(value = "billings", key = "#billingId")
    @Operation(summary = "Delete billing", description = "Deletes billing record and publishes Kafka event")
    public void deleteBilling(Long billingId) {
        log.warn("Deleting billing record with ID: {}", billingId);
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new BillingNotFoundException("Billing record not found with ID: " + billingId));

        billingRepository.delete(billing);

        kafkaTemplate.send(BILLING_TOPIC, "BillingDeleted", billingId);
        log.debug("Kafka event published for deleted billing ID: {}", billingId);
    }
}

