package com.fuel.nexus.dto;

import jakarta.validation.constraints.*;

public class CustomerFeedbackRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Feedback message is required")
    @Size(max = 500, message = "Feedback must not exceed 500 characters")
    private String message;
}

