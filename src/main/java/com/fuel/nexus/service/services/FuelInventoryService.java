package com.fuel.nexus.service.services;

import com.fuel.nexus.dto.FuelInventoryDTO;
import com.fuel.nexus.entity.FuelInventory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


@Tag(name = "Fuel Inventory Service", description = "Service interface for managing fuel inventory (Gas & Liquid)")
public interface FuelInventoryService {

    // Save new fuel inventory record
    @Operation(summary = "Save Fuel Inventory", description = "Create and persist a new fuel inventory record")
    FuelInventory saveFuelInventory(FuelInventoryDTO fuelInventoryDTO);

    // Get all inventory records paged
    @Operation(summary = "Get Fuel Inventory (Paged)", description = "Fetch all fuel inventory records with pagination")
    Page<FuelInventory> getAllFuelInventory(Pageable pageable);

    // Get fuel inventory by ID
    @Operation(summary = "Get Fuel Inventory by ID", description = "Retrieve fuel inventory record by its ID")
    Optional<FuelInventory> getFuelInventoryById(Long inventoryId);

    // Update fuel inventory
    @Operation(summary = "Update Fuel Inventory", description = "Update fuel inventory details such as available quantity and storage location")
    FuelInventory updateFuelInventory(Long inventoryId, FuelInventoryDTO fuelInventoryDTO);

    // Delete fuel inventory by ID
    @Operation(summary = "Delete Fuel Inventory", description = "Delete a fuel inventory record from the system")
    void deleteFuelInventory(Long inventoryId);

    // Find by batch number
    @Operation(summary = "Find by Batch Number", description = "Retrieve fuel inventory by unique batch number")
    Optional<FuelInventory> findByBatchNumber(String batchNumber);

    // Track available quantity
    @Operation(summary = "Track Quantity", description = "Get the available quantity of fuel for a given inventory record")
    Double getAvailableQuantity(Long inventoryId);

    // Restock fuel
    @Operation(summary = "Restock Fuel", description = "Increase stock quantity for a given inventory record")
    FuelInventory restockFuel(Long inventoryId, Double additionalQuantity);

    // Check for low stock alert
    @Operation(summary = "Low Stock Alert", description = "Check if the available stock is below the defined threshold and trigger an alert")
    boolean isLowStock(Long inventoryId, Double threshold);
}

