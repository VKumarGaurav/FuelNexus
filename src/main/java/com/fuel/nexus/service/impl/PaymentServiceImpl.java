package com.fuel.nexus.service.impl;

import com.fuel.nexus.entity.Payment;
import com.fuel.nexus.exception.exceptions.PaymentNotFoundException;
import com.fuel.nexus.repository.PaymentRepository;
import com.fuel.nexus.service.services.PaymentService;
import com.fuel.nexus.utility.PaymentStatus;
import com.fuel.nexus.utility.PaymentMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Payment Service Impl", description = "Implementation of Payment Service with caching, logging, and Kafka integration")
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-events";

    // Create a new payment
    @Override
    @Operation(summary = "Create a new payment", description = "Save a new payment and publish event to Kafka")
    public Payment createPayment(Payment payment) {
        log.info("Creating new payment for billingId: {}", payment.getBillingId());
        Payment savedPayment = paymentRepository.save(payment);
        kafkaTemplate.send(PAYMENT_TOPIC, "Payment created with ID: " + savedPayment.getId());
        return savedPayment;
    }

    // Get payment by ID
    @Override
    @Cacheable(value = "payments", key = "#id")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment by ID with caching support")
    public Optional<Payment> getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);
        return Optional.ofNullable(paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id)));
    }

    // Get all payments (paginated)
    @Override
    @Cacheable(value = "paymentsPage", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Operation(summary = "Get all payments", description = "Retrieve all payments with pagination and caching")
    public Page<Payment> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments with pagination: {}", pageable);
        return paymentRepository.findAll(pageable);
    }

    // Get payments by billing ID
    @Override
    @Cacheable(value = "paymentsByBilling", key = "#billingId")
    @Operation(summary = "Get payments by billing ID", description = "Retrieve all payments linked to a billing record")
    public List<Payment> getPaymentsByBillingId(Long billingId) {
        log.info("Fetching payments for billingId: {}", billingId);
        return paymentRepository.findByBillingId(billingId);
    }

    // Get payments by status
    @Override
    @Cacheable(value = "paymentsByStatus", key = "#status")
    @Operation(summary = "Get payments by status", description = "Retrieve all payments with given status")
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        log.info("Fetching payments with status: {}", status);
        return paymentRepository.findByStatus(status);
    }

    // Get payments by method
    @Override
    @Cacheable(value = "paymentsByMethod", key = "#method")
    @Operation(summary = "Get payments by method", description = "Retrieve all payments done using a specific method")
    public List<Payment> getPaymentsByMethod(PaymentMethod method) {
        log.info("Fetching payments with method: {}", method);
        return paymentRepository.findByMethod(method);
    }

    // Update payment status
    @Override
    @CacheEvict(value = "payments", key = "#paymentId")
    @Operation(summary = "Update payment status", description = "Update status and send Kafka notification")
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        log.info("Updating status of payment with ID: {} to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        payment.setPaymentStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        kafkaTemplate.send(PAYMENT_TOPIC, "Payment status updated for ID: " + updatedPayment.getId() + " to " + status);
        return updatedPayment;
    }

    // Delete payment
    @Override
    @CacheEvict(value = "payments", key = "#paymentId")
    @Operation(summary = "Delete payment", description = "Delete payment and send Kafka notification")
    public void deletePayment(Long paymentId) {
        log.warn("Deleting payment with ID: {}", paymentId);
        if (!paymentRepository.existsById(paymentId)) {
            throw new PaymentNotFoundException("Payment not found with ID: " + paymentId);
        }
        paymentRepository.deleteById(paymentId);
        kafkaTemplate.send(PAYMENT_TOPIC, "Payment deleted with ID: " + paymentId);
    }
}

