package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Billing;
import com.fuel.nexus.utility.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    // Find bills by customer email
    List<Billing> findByCustomerEmail(String customerEmail);

    // Find bills by status (PENDING, PAID, CANCELLED)
    List<Billing> findByStatus(BillingStatus status);

    // Find bills by delivery ID
    List<Billing> findByDeliveryId(Long deliveryId);
}

