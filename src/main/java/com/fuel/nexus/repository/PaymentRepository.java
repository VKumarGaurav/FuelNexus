package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Payment;
import com.fuel.nexus.utility.PaymentMethod;
import com.fuel.nexus.utility.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments by billing ID
    List<Payment> findByBillingId(Long billingId);

    // Find payments by status (SUCCESS, FAILED, PENDING)
    List<Payment> findByStatus(PaymentStatus status);

    // Find payments by method (CASH, CARD, UPI, NET_BANKING)
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    // Find payments by transaction reference
    Payment findByTransactionReference(String transactionReference);

    List<Payment> findByMethod(PaymentMethod method);
}
