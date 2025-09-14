package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Delivery;
import com.fuel.nexus.utility.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Delivery Management.
 * Provides CRUD, pagination, and custom finder methods for deliveries.
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // Custom Finder Methods
    /**
     * Find deliveries by their current status.
     *
     * @param status DeliveryStatus (PENDING, DISPATCHED, DELIVERED, CANCELLED)
     * @return List of matching deliveries
     */
    List<Delivery> findByStatus(DeliveryStatus status);

    /**
     * Find deliveries assigned to a specific agent.
     *
     * @param agentId ID of the delivery agent
     * @return List of deliveries handled by this agent
     */
    List<Delivery> findByAgentId(Long agentId);

    /**
     * Find deliveries assigned to a specific vehicle.
     *
     * @param vehicleId ID of the vehicle
     * @return List of deliveries associated with this vehicle
     */
    List<Delivery> findByVehicleId(Long vehicleId);
}
