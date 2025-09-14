package com.fuel.nexus.controller;

import com.fuel.nexus.dto.CustomerRequestDTO;
import com.fuel.nexus.dto.CustomerResponseDTO;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.service.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer Management
 * Handles CRUD operations for customer profiles
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Controller", description = "APIs for managing customer profiles")
public class CustomerController {

    private final CustomerService customerService;

    // ------------------------------------------------------------------------
    // Endpoint to create a new customer
    // ------------------------------------------------------------------------
    @PostMapping("/create")
    @Operation(
            summary = "Create a new customer",
            description = "Registers a new customer with profile information",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customer created successfully", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO dto) {
        try {
            log.info("Received request to create customer with email={}", dto.getEmail());
            CustomerResponseDTO createdCustomer = customerService.createCustomer(dto);
            log.info("Customer created successfully with id={}", createdCustomer.getId());
            return ResponseEntity.status(201).body(createdCustomer);
        } catch (IllegalArgumentException e) {
            log.error("Invalid customer data: {}", e.getMessage(), e);
            throw new InvalidRequestException("Invalid customer input: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // Endpoint to update an existing customer
    // ------------------------------------------------------------------------
    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer's profile details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer updated successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO dto
    ) {
        log.info("Received request to update customer with id={}", id);
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, dto);

        if (updatedCustomer == null) {
            log.warn("Customer not found with id={}", id);
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }

        log.info("Customer updated successfully with id={}", updatedCustomer.getId());
        return ResponseEntity.ok(updatedCustomer);
    }

    // ------------------------------------------------------------------------
    // Endpoint to fetch a customer by ID
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(
            summary = "Get customer by ID",
            description = "Fetches a customer's profile by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer found", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        log.info("Fetching customer with id={}", id);
        CustomerResponseDTO customer = customerService.getCustomerById(id);

        if (customer == null) {
            log.warn("Customer not found with id={}", id);
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }

        log.info("Customer retrieved successfully with id={}", id);
        return ResponseEntity.ok(customer);
    }

    // ------------------------------------------------------------------------
    // Endpoint to fetch all customers
    // ------------------------------------------------------------------------
    @GetMapping("/all")
    @Operation(
            summary = "Get all customers",
            description = "Retrieves a list of all registered customers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of customers returned successfully", content = @Content)
            }
    )
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        log.info("Fetching all customers");
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();

        if (customers.isEmpty()) {
            log.warn("No customers found in the system");
            throw new ResourceNotFoundException("No customers found");
        }

        log.info("Retrieved {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    // ------------------------------------------------------------------------
    // Endpoint to delete a customer by ID
    // ------------------------------------------------------------------------
    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Delete customer",
            description = "Deletes a customer by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            }
    )
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        log.info("Received request to delete customer with id={}", id);
        try {
            customerService.deleteCustomer(id);
            log.info("Customer deleted successfully with id={}", id);
            return ResponseEntity.ok("Customer deleted successfully with id: " + id);
        } catch (IllegalStateException e) {
            log.error("Error deleting customer with id={}: {}", id, e.getMessage(), e);
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
    }
}
