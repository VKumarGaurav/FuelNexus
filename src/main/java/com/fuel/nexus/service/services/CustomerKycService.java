package com.fuel.nexus.service.services;

import com.fuel.nexus.dto.CustomerKycDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Tag(name = "Customer KYC Management", description = "APIs for managing customer identity proofs (KYC)")
public interface CustomerKycService {

    /**
     * Save KYC details for a customer.
     */
    @Operation(
            summary = "Save Customer KYC",
            description = "Stores customer KYC information such as Aadhaar and PAN details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC details saved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid KYC data"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    CustomerKycDTO saveCustomerKyc(CustomerKycDTO customerKycDTO);

    /**
     * Get KYC details of a customer by customer ID.
     */
    @Operation(
            summary = "Get Customer KYC by Customer ID",
            description = "Fetches the KYC details for a given customer ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC details retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer KYC not found")
            }
    )
    CustomerKycDTO getKycByCustomerId(Long customerId);

    /**
     * Get all KYC records.
     */
    @Operation(
            summary = "Get All Customer KYC Records",
            description = "Retrieves all customer KYC entries",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of KYC records retrieved successfully")
            }
    )
    List<CustomerKycDTO> getAllKycRecords();

    /**
     * Delete a customer's KYC record.
     */
    @Operation(
            summary = "Delete Customer KYC",
            description = "Deletes the KYC record for a given customer ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "KYC deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "KYC record not found")
            }
    )
    void deleteKyc(Long customerId);
}

