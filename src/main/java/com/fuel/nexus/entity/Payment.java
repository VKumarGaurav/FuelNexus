package com.fuel.nexus.entity;

import com.fuel.nexus.utility.PaymentMethod;
import com.fuel.nexus.utility.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Billing ID is required")
    private Long billingId;

    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "CASH|CARD|UPI|NET_BANKING", message = "Payment method must be CASH, CARD, UPI, or NET_BANKING")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than 0")
    private Double paymentAmount;

    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime paymentDate;

    @NotBlank(message = "Payment status is required")
    @Pattern(regexp = "SUCCESS|FAILED|PENDING", message = "Status must be SUCCESS, FAILED, or PENDING")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Size(max = 255, message = "Transaction reference must not exceed 255 characters")
    private String transactionReference;
}

