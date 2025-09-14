package com.fuel.nexus.controller;

import com.fuel.nexus.dto.CustomerKycDTO;
import com.fuel.nexus.service.services.CustomerKycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer KYC Management
 * Handles creation, retrieval, and deletion of customer KYC records
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-kyc")
@RequiredArgsConstructor
@Tag(name = "Customer KYC Controller", description = "APIs for managing customer identity proofs (KYC)")
public class CustomerKycController {

    private final CustomerKycService kycService;

    // ------------------------------------------------------------------------
    // Endpoint to save KYC details
    // ------------------------------------------------------------------------
    @PostMapping("/save")
    @Operation(
            summary = "Save Customer KYC",
            description = "Stores customer KYC information such as Aadhaar and PAN details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC saved successfully", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid KYC data", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            }
    )
    public ResponseEntity<CustomerKycDTO> saveCustomerKyc(@Valid @RequestBody CustomerKycDTO kycDTO) {
        log.info("Received request to save KYC for customerId={}", kycDTO.getCustomerId());
        CustomerKycDTO savedKyc = kycService.saveCustomerKyc(kycDTO);
        log.info("KYC saved successfully with id={}", savedKyc.getId());
        return ResponseEntity.ok(savedKyc);
    }

    // ------------------------------------------------------------------------
    // Endpoint to get KYC by customer ID
    // ------------------------------------------------------------------------
    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Get Customer KYC by Customer ID",
            description = "Fetches the KYC details for a given customer ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC retrieved successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Customer KYC not found", content = @Content)
            }
    )
    public ResponseEntity<CustomerKycDTO> getKycByCustomerId(@PathVariable Long customerId) {
        log.info("Fetching KYC for customerId={}", customerId);
        CustomerKycDTO kycDTO = kycService.getKycByCustomerId(customerId);
        log.info("KYC retrieved successfully for customerId={}", customerId);
        return ResponseEntity.ok(kycDTO);
    }

    // ------------------------------------------------------------------------
    // Endpoint to get all KYC records
    // ------------------------------------------------------------------------
    @GetMapping("/all")
    @Operation(
            summary = "Get All Customer KYC Records",
            description = "Retrieves all customer KYC entries",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of KYC records retrieved successfully", content = @Content)
            }
    )
    public ResponseEntity<List<CustomerKycDTO>> getAllKycRecords() {
        log.info("Fetching all KYC records");
        List<CustomerKycDTO> kycList = kycService.getAllKycRecords();
        log.info("Retrieved {} KYC records", kycList.size());
        return ResponseEntity.ok(kycList);
    }

    // ------------------------------------------------------------------------
    // Endpoint to delete KYC by customer ID
    // ------------------------------------------------------------------------
    @DeleteMapping("/delete/{customerId}")
    @Operation(
            summary = "Delete Customer KYC",
            description = "Deletes the KYC record for a given customer ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "KYC record not found", content = @Content)
            }
    )
    public ResponseEntity<String> deleteKyc(@PathVariable Long customerId) {
        log.info("Received request to delete KYC for customerId={}", customerId);
        kycService.deleteKyc(customerId);
        log.info("KYC deleted successfully for customerId={}", customerId);
        return ResponseEntity.ok("KYC deleted successfully for customerId: " + customerId);
    }
}
