package com.fuel.nexus.repository;

import com.fuel.nexus.entity.FuelInventory;
import com.fuel.nexus.utility.FuelType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;

@Repository
public interface FuelInventoryRepository extends JpaRepository<FuelInventory, Long> {

    FuelInventory findByBatchNumber(String batchNumber);

    boolean existsByBatchNumber(String batchNumber);

    <T> ScopedValue<T> findByFuelType(@NotBlank(message = "Type  cannot be blank") FuelType fuelType);
}

