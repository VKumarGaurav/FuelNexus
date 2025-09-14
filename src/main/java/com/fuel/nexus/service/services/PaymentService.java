package com.fuel.nexus.service.services;

import com.fuel.nexus.entity.Payment;
import com.fuel.nexus.utility.PaymentStatus;
import com.fuel.nexus.utility.PaymentMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Tag(name = "Payment Service", description = "APIs for managing payment operations")
public interface PaymentService {

    // Create a new payment
    @Operation(summary = "Create a new payment", description = "Initiates a payment for a billing record")
    Payment createPayment(Payment payment);

    // Get payment by ID
    @Operation(summary = "Get payment by ID", description = "Fetch a payment record using its unique ID")
    Optional<Payment> getPaymentById(Long id);

    // Get all payments (paginated)
    @Operation(summary = "Get all payments (paginated)", description = "Retrieve all payment records with pagination support")
    Page<Payment> getAllPayments(Pageable pageable);

    // Get payments by billing ID
    @Operation(summary = "Get payments by billing ID", description = "Fetch all payments linked to a specific billing record")
    List<Payment> getPaymentsByBillingId(Long billingId);

    // Get payments by status
    @Operation(summary = "Get payments by status", description = "Fetch payment records based on their status")
    List<Payment> getPaymentsByStatus(PaymentStatus status);

    // Get payments by method
    @Operation(summary = "Get payments by method", description = "Fetch payments made using a specific method (CASH, CARD, UPI, etc.)")
    List<Payment> getPaymentsByMethod(PaymentMethod method);

    // Update payment status
    @Operation(summary = "Update payment status", description = "Update the status of a payment record (SUCCESS, FAILED, PENDING)")
    Payment updatePaymentStatus(Long paymentId, PaymentStatus status);

    // Delete payment
    @Operation(summary = "Delete payment", description = "Remove a payment record by ID")
    void deletePayment(Long paymentId);
}
