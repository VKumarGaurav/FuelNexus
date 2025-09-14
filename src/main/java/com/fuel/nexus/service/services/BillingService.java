package com.fuel.nexus.service.services;

import com.fuel.nexus.entity.Billing;
import com.fuel.nexus.utility.BillingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Tag(name = "Billing Service", description = "APIs for managing billing operations")
public interface BillingService {

    // Create a new billing record
    @Operation(summary = "Create a new billing record", description = "Generates a billing entry for a completed booking or delivery")
    Billing createBilling(Billing billing);

    // Get billing by ID
    @Operation(summary = "Get billing by ID", description = "Fetch a billing record using its unique ID")
    Optional<Billing> getBillingById(Long id);

    // Get all billings (paginated)
    @Operation(summary = "Get all billings (paginated)", description = "Retrieve all billing records with pagination support")
    Page<Billing> getAllBillings(Pageable pageable);

    // Get billings by customer email
    @Operation(summary = "Get billings by customer email", description = "Fetch billing records associated with a customer's email")
    List<Billing> getBillingsByCustomerEmail(String customerEmail);

    // Get billings by status
    @Operation(summary = "Get billings by status", description = "Fetch billing records based on their status")
    List<Billing> getBillingsByStatus(BillingStatus status);

    // Update billing status
    @Operation(summary = "Update billing status", description = "Update the status of a billing record (e.g., PAID, PENDING, CANCELLED)")
    Billing updateBillingStatus(Long billingId, BillingStatus status);

    // Delete billing
    @Operation(summary = "Delete billing", description = "Remove a billing record by ID")
    void deleteBilling(Long billingId);
}

