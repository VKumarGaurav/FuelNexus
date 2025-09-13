package com.fuel.nexus.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CustomerKycDTO {

    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Aadhar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Aadhar must be a valid 12-digit number starting with 2-9")
    private String aadharNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be valid (e.g., ABCDE1234F)")
    private String panNumber;

    @PastOrPresent(message = "KYC issue date cannot be in the future")
    @NotNull(message = "KYC issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "KYC active status is required")
    private Boolean active;
}

