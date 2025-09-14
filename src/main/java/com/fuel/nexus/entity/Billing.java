package com.fuel.nexus.entity;

import com.fuel.nexus.utility.BillingStatus;
import com.fuel.nexus.utility.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "billings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Delivery ID is required")
    private Long deliveryId;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(min = 3, max = 100, message = "Customer name must be between 3 and 100 characters")
    private String customerName;

    @NotBlank(message = "Customer email cannot be blank")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotNull(message = "Billing amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Billing amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Billing date is required")
    @PastOrPresent(message = "Billing date cannot be in the future")
    private LocalDateTime billingDate;

    @NotBlank(message = "Billing status is required")
    @Pattern(regexp = "PENDING|PAID|CANCELLED", message = "Status must be PENDING, PAID, or CANCELLED")
    @Enumerated(EnumType.STRING)
    private BillingStatus billingStatus;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
