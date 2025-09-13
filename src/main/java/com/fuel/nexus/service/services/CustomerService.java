package com.fuel.nexus.service.services;


import com.fuel.nexus.dto.CustomerRequestDTO;
import com.fuel.nexus.dto.CustomerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Tag(name = "Customer Management", description = "Customer profile operations")
public interface CustomerService {

    // Create a new customer
    @Operation(
            summary = "Create a new customer",
            description = "Registers a new customer with profile information",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customer created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    CustomerResponseDTO createCustomer(CustomerRequestDTO dto);

    // Update existing customer by ID
    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer's profile details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto);

    // Fetch customer by ID
    @Operation(
            summary = "Get customer by ID",
            description = "Fetches a customer's profile by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer found"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    CustomerResponseDTO getCustomerById(Long id);

    // Fetch all customers
    @Operation(
            summary = "Get all customers",
            description = "Retrieves a list of all registered customers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of customers returned successfully")
            }
    )
    List<CustomerResponseDTO> getAllCustomers();

    // Delete customer by ID
    @Operation(
            summary = "Delete customer",
            description = "Deletes a customer by their ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    void deleteCustomer(Long id);
}

