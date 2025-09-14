package com.fuel.nexus.controller;

import com.fuel.nexus.entity.Payment;
import com.fuel.nexus.exception.exceptions.PaymentNotFoundException;
import com.fuel.nexus.service.services.PaymentService;
import com.fuel.nexus.utility.PaymentStatus;
import com.fuel.nexus.utility.PaymentMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "APIs for managing Payment operations with Kafka, Caching, and Exception handling")
public class PaymentController {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-events";

    // ---------------- Create Payment ----------------
    @PostMapping
    @Operation(
            summary = "Create a new payment",
            description = "Save a new payment and publish event to Kafka",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        log.info("Request to create new payment for billingId: {}", payment.getBillingId());
        Payment created = paymentService.createPayment(payment);
        kafkaTemplate.send(PAYMENT_TOPIC, "PaymentCreated: " + created.getId());
        return ResponseEntity.ok(created);
    }

    // ---------------- Get Payment by ID ----------------
    @GetMapping("/{id}")
    @Cacheable(value = "payments", key = "#id")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve payment record by ID with caching support",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
            }
    )
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        log.info("Request to fetch payment by ID: {}", id);
        Payment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));
        return ResponseEntity.ok(payment);
    }

    // ---------------- Get All Payments ----------------
    @GetMapping
    @Operation(
            summary = "Get all payments (paginated)",
            description = "Retrieve all payments with pagination and caching",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payments fetched successfully",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<Page<Payment>> getAllPayments(Pageable pageable) {
        log.info("Request to fetch all payments with pagination: {}", pageable);
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    // ---------------- Get Payments by Billing ID ----------------
    @GetMapping("/billing/{billingId}")
    @Cacheable(value = "paymentsByBilling", key = "#billingId")
    @Operation(
            summary = "Get payments by Billing ID",
            description = "Retrieve all payments linked to a specific billing record",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payments found",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<List<Payment>> getPaymentsByBillingId(@PathVariable Long billingId) {
        log.info("Request to fetch payments by billingId: {}", billingId);
        return ResponseEntity.ok(paymentService.getPaymentsByBillingId(billingId));
    }

    // ---------------- Get Payments by Status ----------------
    @GetMapping("/status/{status}")
    @Cacheable(value = "paymentsByStatus", key = "#status")
    @Operation(
            summary = "Get payments by status",
            description = "Retrieve all payments filtered by status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payments found",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.info("Request to fetch payments by status: {}", status);
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    // ---------------- Get Payments by Method ----------------
    @GetMapping("/method/{method}")
    @Cacheable(value = "paymentsByMethod", key = "#method")
    @Operation(
            summary = "Get payments by method",
            description = "Retrieve all payments filtered by method (e.g. CASH, CARD, UPI)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payments found",
                            content = @Content(mediaType = "application/json"))
            }
    )
    public ResponseEntity<List<Payment>> getPaymentsByMethod(@PathVariable PaymentMethod method) {
        log.info("Request to fetch payments by method: {}", method);
        return ResponseEntity.ok(paymentService.getPaymentsByMethod(method));
    }

    // ---------------- Update Payment Status ----------------
    @PutMapping("/{id}/status")
    @CacheEvict(value = "payments", key = "#id")
    @Operation(
            summary = "Update payment status",
            description = "Update the status of a payment and publish Kafka event",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
            }
    )
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        log.info("Request to update status of payment ID: {} to {}", id, status);
        Payment updated = paymentService.updatePaymentStatus(id, status);
        kafkaTemplate.send(PAYMENT_TOPIC, "PaymentStatusUpdated: " + updated.getId() + " to " + status);
        return ResponseEntity.ok(updated);
    }

    // ---------------- Delete Payment ----------------
    @DeleteMapping("/{id}")
    @CacheEvict(value = "payments", key = "#id")
    @Operation(
            summary = "Delete payment",
            description = "Delete a payment record and publish Kafka event",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.warn("Request to delete payment with ID: {}", id);
        paymentService.deletePayment(id);
        kafkaTemplate.send(PAYMENT_TOPIC, "PaymentDeleted: " + id);
        return ResponseEntity.noContent().build();
    }
}

