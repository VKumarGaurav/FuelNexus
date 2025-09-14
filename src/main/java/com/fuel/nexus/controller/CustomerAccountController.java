package com.fuel.nexus.controller;

import com.fuel.nexus.dto.CustomerAccountRequestDTO;
import com.fuel.nexus.exception.exceptions.NotAuthorizedException;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.service.services.CustomerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Customer Account Management
 * Handles account creation and login validation
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-account")
@RequiredArgsConstructor
@Tag(name = "Customer Account Controller", description = "APIs for creating and authenticating customer accounts")
public class CustomerAccountController {

    private final CustomerAccountService accountService;

    // ------------------------------------------------------------------------
    // Endpoint to create a new customer account
    // ------------------------------------------------------------------------
    @PostMapping("/create")
    @Operation(
            summary = "Create Customer Account",
            description = "Creates a new login account for a registered customer. " +
                    "Returns 201 on success, 409 if username exists.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created successfully"),
                    @ApiResponse(responseCode = "409", description = "Username already exists"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data")
            }
    )
    public ResponseEntity<String> createAccount(@Valid @RequestBody CustomerAccountRequestDTO dto) {
        try {
            log.info("Received request to create account for username={}", dto.getUsername());
            accountService.createAccount(dto);
            log.info("Account created successfully for username={}", dto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Account created successfully for username: " + dto.getUsername());
        } catch (IllegalArgumentException e) {
            log.error("Invalid account creation request: {}", e.getMessage(), e);
            throw new InvalidRequestException("Invalid input: " + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Account creation failed: {}", e.getMessage());
            throw new InvalidRequestException("Username already exists: " + dto.getUsername());
        }
    }

    // ------------------------------------------------------------------------
    // Endpoint to validate customer login
    // ------------------------------------------------------------------------
    @PostMapping("/login")
    @Operation(
            summary = "Validate Customer Login",
            description = "Validates username and password for customer login. " +
                    "Returns 200 on success, 401 if credentials are invalid.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password) {
        log.info("Received login request for username={}", username);
        try {
            boolean success = accountService.validateLogin(username, password);
            if (success) {
                log.info("Login successful for username={}", username);
                return ResponseEntity.ok("Login successful for username: " + username);
            } else {
                log.warn("Login failed - invalid credentials for username={}", username);
                throw new NotAuthorizedException("Invalid credentials for username: " + username);
            }
        } catch (ResourceNotFoundException e) {
            log.error("Login failed - user not found: {}", username);
            throw e; // handled by GlobalExceptionHandler
        }
    }
}
