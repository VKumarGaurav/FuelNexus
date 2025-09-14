package com.fuel.nexus.controller;

import com.fuel.nexus.entity.Billing;
import com.fuel.nexus.exception.exceptions.BillingNotFoundException;
import com.fuel.nexus.service.services.BillingService;
import com.fuel.nexus.utility.BillingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
@Tag(
        name = "Billing Controller",
        description = "REST APIs for managing billing records, including CRUD operations, status updates, and retrieval"
)
public class BillingController {

    private final BillingService billingService;

    // ------------------------------------------------------------------------
    // Create a new billing record
    // ------------------------------------------------------------------------
    @PostMapping
    @Operation(
            summary = "Create billing",
            description = "Create a new billing record and publish event to Kafka",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Billing created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Billing.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    public ResponseEntity<Billing> createBilling(@RequestBody Billing billing) {
        log.info("REST request to create new billing record for customer: {}", billing.getCustomerEmail());
        Billing created = billingService.createBilling(billing);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ------------------------------------------------------------------------
    // Get billing by ID
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(
            summary = "Get billing by ID",
            description = "Retrieve billing record by ID (uses cache)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billing record retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Billing.class))),
                    @ApiResponse(responseCode = "404", description = "Billing record not found", content = @Content)
            }
    )
    public ResponseEntity<Billing> getBillingById(@PathVariable Long id) {
        log.info("REST request to fetch billing by ID: {}", id);
        return billingService.getBillingById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found with ID: " + id));
    }

    // ------------------------------------------------------------------------
    // Get all billings (paginated)
    // ------------------------------------------------------------------------
    @GetMapping
    @Operation(
            summary = "Get all billings (paginated)",
            description = "Retrieve all billing records with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billing records retrieved successfully",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<Page<Billing>> getAllBillings(Pageable pageable) {
        log.info("REST request to fetch all billings with pagination: {}", pageable);
        return ResponseEntity.ok(billingService.getAllBillings(pageable));
    }

    // ------------------------------------------------------------------------
    // Get billings by customer email
    // ------------------------------------------------------------------------
    @GetMapping("/customer/{email}")
    @Operation(
            summary = "Get billings by customer email",
            description = "Retrieve billing records for a specific customer email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billing records retrieved successfully",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<List<Billing>> getBillingsByCustomerEmail(@PathVariable String email) {
        log.info("REST request to fetch billings for customer email: {}", email);
        return ResponseEntity.ok(billingService.getBillingsByCustomerEmail(email));
    }

    // ------------------------------------------------------------------------
    // Get billings by status
    // ------------------------------------------------------------------------
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get billings by status",
            description = "Retrieve billing records filtered by status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billing records retrieved successfully",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<List<Billing>> getBillingsByStatus(@PathVariable BillingStatus status) {
        log.info("REST request to fetch billings with status: {}", status);
        return ResponseEntity.ok(billingService.getBillingsByStatus(status));
    }

    // ------------------------------------------------------------------------
    // Update billing status
    // ------------------------------------------------------------------------
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update billing status",
            description = "Update the status of a billing record and publish Kafka event",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Billing status updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Billing.class))),
                    @ApiResponse(responseCode = "404", description = "Billing record not found", content = @Content)
            }
    )
    public ResponseEntity<Billing> updateBillingStatus(@PathVariable Long id,
                                                       @RequestParam BillingStatus status) {
        log.info("REST request to update billing ID {} to status {}", id, status);
        return ResponseEntity.ok(billingService.updateBillingStatus(id, status));
    }

    // ------------------------------------------------------------------------
    // Delete billing record
    // ------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete billing",
            description = "Delete a billing record and publish Kafka event",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Billing deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Billing record not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteBilling(@PathVariable Long id) {
        log.warn("REST request to delete billing record with ID: {}", id);
        billingService.deleteBilling(id);
        return ResponseEntity.noContent().build();
    }
}
