package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.FuelInventoryDTO;
import com.fuel.nexus.entity.FuelInventory;
import com.fuel.nexus.exception.exceptions.OutOfStockException;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.FuelInventoryRepository;
import com.fuel.nexus.service.services.FuelInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Tag(name = "Fuel Inventory Service", description = "Service implementation for managing fuel inventory (Gas & Liquid)")
public class FuelInventoryServiceImpl implements FuelInventoryService {

    private final FuelInventoryRepository fuelInventoryRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String FUEL_TOPIC = "fuel-inventory-topic";

    /**
     * Save new fuel inventory record
     */
    @Override
    @Operation(summary = "Save Fuel Inventory", description = "Create and persist a new fuel inventory record")
    @CacheEvict(value = "fuelInventoryCache", allEntries = true) // Clear cache on save
    public FuelInventory saveFuelInventory(FuelInventoryDTO dto) {
        log.info("Saving new fuel inventory for batchNumber={}", dto.getBatchNumber());

        FuelInventory inventory = modelMapper.map(dto, FuelInventory.class);
        FuelInventory savedInventory = fuelInventoryRepository.save(inventory);

        log.info("Fuel inventory saved with ID={}", savedInventory.getId());

        // Kafka notification
        kafkaTemplate.send(FUEL_TOPIC, "New fuel inventory added for batch: " + savedInventory.getBatchNumber());

        return savedInventory;
    }

    /**
     * Get all inventory records paged
     */
    @Override
    @Operation(summary = "Get Fuel Inventory (Paged)", description = "Fetch all fuel inventory records with pagination")
    @Cacheable(value = "fuelInventoryCache", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<FuelInventory> getAllFuelInventory(Pageable pageable) {
        log.info("Fetching fuel inventory page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return fuelInventoryRepository.findAll(pageable);
    }

    /**
     * Get fuel inventory by ID
     */
    @Override
    @Operation(summary = "Get Fuel Inventory by ID", description = "Retrieve fuel inventory record by its ID")
    @Cacheable(value = "fuelInventoryCache", key = "#inventoryId")
    public Optional<FuelInventory> getFuelInventoryById(Long inventoryId) {
        log.info("Fetching fuel inventory with ID={}", inventoryId);
        return fuelInventoryRepository.findById(inventoryId);
    }

    /**
     * Update fuel inventory
     */
    @Override
    @Operation(summary = "Update Fuel Inventory", description = "Update fuel inventory details such as available quantity and storage location")
    @CacheEvict(value = "fuelInventoryCache", allEntries = true)
    public FuelInventory updateFuelInventory(Long inventoryId, FuelInventoryDTO dto) {
        log.info("Updating fuel inventory ID={}", inventoryId);

        FuelInventory inventory = fuelInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + inventoryId));

        // Update fields
        inventory.setFuelType(dto.getFuelType());
        inventory.setBatchNumber(dto.getBatchNumber());
        inventory.setAvailableQuantity(dto.getAvailableQuantity());
        inventory.setStorageLocation(dto.getStorageLocation());

        FuelInventory updatedInventory = fuelInventoryRepository.save(inventory);
        log.info("Fuel inventory updated ID={}", updatedInventory.getId());

        // Kafka notification
        kafkaTemplate.send(FUEL_TOPIC, "Fuel inventory updated for batch: " + updatedInventory.getBatchNumber());

        return updatedInventory;
    }

    /**
     * Delete fuel inventory by ID
     */
    @Override
    @Operation(summary = "Delete Fuel Inventory", description = "Delete a fuel inventory record from the system")
    @CacheEvict(value = "fuelInventoryCache", allEntries = true)
    public void deleteFuelInventory(Long inventoryId) {
        log.info("Deleting fuel inventory ID={}", inventoryId);

        FuelInventory inventory = fuelInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + inventoryId));

        fuelInventoryRepository.delete(inventory);
        log.info("Fuel inventory deleted ID={}", inventoryId);

        kafkaTemplate.send(FUEL_TOPIC, "Fuel inventory deleted for batch: " + inventory.getBatchNumber());
    }

    /**
     * Find by batch number
     */
    @Override
    @Operation(summary = "Find by Batch Number", description = "Retrieve fuel inventory by unique batch number")
    public Optional<FuelInventory> findByBatchNumber(String batchNumber) {
        log.info("Fetching fuel inventory for batchNumber={}", batchNumber);
        return Optional.ofNullable(fuelInventoryRepository.findByBatchNumber(batchNumber));
    }

    /**
     * Track available quantity
     */
    @Override
    @Operation(summary = "Track Quantity", description = "Get the available quantity of fuel for a given inventory record")
    public Double getAvailableQuantity(Long inventoryId) {
        log.info("Checking available quantity for inventory ID={}", inventoryId);

        FuelInventory inventory = fuelInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + inventoryId));

        return inventory.getAvailableQuantity();
    }

    /**
     * Restock fuel
     */
    @Override
    @Operation(summary = "Restock Fuel", description = "Increase stock quantity for a given inventory record")
    @CacheEvict(value = "fuelInventoryCache", allEntries = true)
    public FuelInventory restockFuel(Long inventoryId, Double additionalQuantity) {
        log.info("Restocking fuel inventory ID={} with quantity={}", inventoryId, additionalQuantity);

        FuelInventory inventory = fuelInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + inventoryId));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + additionalQuantity);
        FuelInventory updatedInventory = fuelInventoryRepository.save(inventory);

        log.info("Fuel inventory restocked ID={} newQuantity={}", inventoryId, updatedInventory.getAvailableQuantity());

        // Kafka notification if stock is low
        if (isLowStock(inventoryId, 50.0)) { // Example threshold
            kafkaTemplate.send(FUEL_TOPIC, "Low stock alert for batch: " + inventory.getBatchNumber());
            log.warn("Low stock alert triggered for inventory ID={}", inventoryId);
        }

        return updatedInventory;
    }

    /**
     * Check for low stock alert
     */
    @Override
    @Operation(summary = "Low Stock Alert", description = "Check if the available stock is below the defined threshold and trigger an alert")
    public boolean isLowStock(Long inventoryId, Double threshold) {
        FuelInventory inventory = fuelInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + inventoryId));

        boolean lowStock = inventory.getAvailableQuantity() < threshold;
        if (lowStock) {
            log.warn("Low stock detected for inventory ID={} quantity={}", inventoryId, inventory.getAvailableQuantity());
        }
        return lowStock;
    }
}
