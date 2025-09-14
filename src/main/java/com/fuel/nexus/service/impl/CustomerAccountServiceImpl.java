package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.CustomerAccountRequestDTO;
import com.fuel.nexus.entity.CustomerAccount;
import com.fuel.nexus.exception.exceptions.DuplicateUsernameException;
import com.fuel.nexus.exception.exceptions.NotAuthorizedException;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.CustomerAccountRepository;
import com.fuel.nexus.service.services.CustomerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(
        name = "Customer Account Management",
        description = "Handles customer account creation, login validation, and authentication"
)
public class CustomerAccountServiceImpl implements CustomerAccountService {

    private final CustomerAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_ACCOUNT_CREATED = "customer-account-events";

    // ------------------------------------------------------------------------
    // Create a new customer account (linked with customer profile)
    // ------------------------------------------------------------------------
    @Override
    @Operation(
            summary = "Create customer account",
            description = "Creates a new login account for a registered customer. " +
                    "Throws DuplicateUsernameException if username already exists.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created successfully"),
                    @ApiResponse(responseCode = "409", description = "Username already exists"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data")
            }
    )
    public void createAccount(CustomerAccountRequestDTO dto) {
        log.info("Attempting to create account for username: {}", dto.getUsername());

        // Check if username already exists
        Optional<CustomerAccount> existing = accountRepository.findByUsername(dto.getUsername());
        if (existing.isPresent()) {
            log.warn("Account creation failed. Username {} already exists.", dto.getUsername());
            throw new DuplicateUsernameException("Username already exists: " + dto.getUsername());
        }

        // Create and save account
        CustomerAccount account = new CustomerAccount();
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setCreatedDate(LocalDate.from(LocalDateTime.now()));

        accountRepository.save(account);
        log.info("Account created successfully for username: {}", dto.getUsername());

        // Publish Kafka event
        kafkaTemplate.send(TOPIC_ACCOUNT_CREATED, "Account created for username: " + dto.getUsername());
        log.debug("Kafka event sent for account creation, topic: {}", TOPIC_ACCOUNT_CREATED);
    }

    // ------------------------------------------------------------------------
    // Validate login credentials
    // ------------------------------------------------------------------------
    @Override
    @Operation(
            summary = "Validate customer login",
            description = "Validates username and password for customer login. " +
                    "Throws NotAuthorizedException if credentials are invalid.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @Cacheable(value = "loginCache", key = "#username") // Cacheable for performance
    public boolean validateLogin(String username, String password) {
        log.info("Validating login for username: {}", username);

        // Fetch user account
        CustomerAccount account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed: Username {} not found", username);
                    return new ResourceNotFoundException("User not found: " + username);
                });

        // Verify password
        boolean matches = passwordEncoder.matches(password, account.getPassword());
        if (!matches) {
            log.warn("Login failed: Invalid password for username {}", username);
            throw new NotAuthorizedException("Invalid credentials for username: " + username);
        }

        log.info("Login successful for username: {}", username);
        return true;
    }
}

