package com.fuel.nexus.service.services;

import com.fuel.nexus.dto.CustomerAccountRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "Customer Account Management", description = "Customer login and account operations")
public interface CustomerAccountService {

    // Create a new customer account (linked with customer profile)
    @Operation(
            summary = "Create customer account",
            description = "Creates a new login account for a registered customer",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
            }
    )
    void createAccount(CustomerAccountRequestDTO dto);

    // Validate login credentials
    @Operation(
            summary = "Validate customer login",
            description = "Validates username and password for customer login",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    boolean validateLogin(String username, String password);
}

