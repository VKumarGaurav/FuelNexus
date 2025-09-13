package com.fuel.nexus.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CustomerFeedbackDTO {

    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Feedback message cannot be empty")
    @Size(min = 10, max = 500, message = "Feedback message must be between 10 and 500 characters")
    private String message;

    @PastOrPresent(message = "Feedback date cannot be in the future")
    @NotNull(message = "Feedback date is required")
    private LocalDateTime feedbackDate;

    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String contactEmail;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Contact number must be a valid 10-digit Indian mobile number")
    private String contactNumber;
}

