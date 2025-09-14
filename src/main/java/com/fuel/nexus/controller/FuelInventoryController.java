package com.fuel.nexus.controller;

import com.fuel.nexus.dto.FuelInventoryDTO;
import com.fuel.nexus.entity.FuelInventory;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.service.services.FuelInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.requests.ApiError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Fuel Inventory Management
 * Handles CRUD operations, stock tracking, and low stock alerts
 */
@Slf4j
@RestController
@RequestMapping("/api/fuel-inventory")
@RequiredArgsConstructor
@Tag(name = "Fuel Inventory Controller", description = "APIs for managing fuel inventory records")
public class FuelInventoryController {

    private final FuelInventoryService fuelInventoryService;

    // ------------------------------------------------------------------------
    // Create a new fuel inventory record
    // ------------------------------------------------------------------------
    @PostMapping("/create")
    @Operation(
            summary = "Create Fuel Inventory",
            description = "Add a new fuel inventory record",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Fuel inventory created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FuelInventory.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<FuelInventory> createFuelInventory(@Valid @RequestBody FuelInventoryDTO dto) {
        log.info("Received request to create fuel inventory for batchNumber={}", dto.getBatchNumber());
        FuelInventory savedInventory = fuelInventoryService.saveFuelInventory(dto);
        log.info("Fuel inventory created with ID={}", savedInventory.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInventory);
    }

    // ------------------------------------------------------------------------
    // Fetch all fuel inventory records (paged)
    // ------------------------------------------------------------------------
    @GetMapping("/all")
    @Operation(
            summary = "Get Fuel Inventory (Paged)",
            description = "Retrieve all fuel inventory records with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paged fuel inventory returned successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
            }
    )
    public ResponseEntity<Page<FuelInventory>> getAllFuelInventory(Pageable pageable) {
        log.info("Fetching fuel inventory page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<FuelInventory> inventoryPage = fuelInventoryService.getAllFuelInventory(pageable);
        return ResponseEntity.ok(inventoryPage);
    }

    // ------------------------------------------------------------------------
    // Fetch fuel inventory by ID
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(
            summary = "Get Fuel Inventory by ID",
            description = "Retrieve fuel inventory record by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fuel inventory found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FuelInventory.class))),
                    @ApiResponse(responseCode = "404", description = "Fuel inventory not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<FuelInventory> getFuelInventoryById(@PathVariable Long id) {
        log.info("Fetching fuel inventory with ID={}", id);
        Optional<FuelInventory> inventory = fuelInventoryService.getFuelInventoryById(id);
        return inventory.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel inventory not found with ID: " + id));
    }

    // ------------------------------------------------------------------------
    // Update fuel inventory
    // ------------------------------------------------------------------------
    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update Fuel Inventory",
            description = "Update details like available quantity and storage location",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fuel inventory updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FuelInventory.class))),
                    @ApiResponse(responseCode = "404", description = "Fuel inventory not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<FuelInventory> updateFuelInventory(
            @PathVariable Long id,
            @Valid @RequestBody FuelInventoryDTO dto
    ) {
        log.info("Received request to update fuel inventory ID={}", id);
        FuelInventory updatedInventory = fuelInventoryService.updateFuelInventory(id, dto);
        log.info("Fuel inventory updated ID={}", updatedInventory.getId());
        return ResponseEntity.ok(updatedInventory);
    }

    // ------------------------------------------------------------------------
    // Delete fuel inventory
    // ------------------------------------------------------------------------
    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Delete Fuel Inventory",
            description = "Delete a fuel inventory record",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Fuel inventory deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Fuel inventory not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<Void> deleteFuelInventory(@PathVariable Long id) {
        log.info("Received request to delete fuel inventory ID={}", id);
        fuelInventoryService.deleteFuelInventory(id);
        log.info("Fuel inventory deleted ID={}", id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------
    // Restock fuel
    // ------------------------------------------------------------------------
    @PostMapping("/restock/{id}")
    @Operation(
            summary = "Restock Fuel",
            description = "Increase stock quantity for a given inventory record",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fuel inventory restocked successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FuelInventory.class))),
                    @ApiResponse(responseCode = "404", description = "Fuel inventory not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<FuelInventory> restockFuel(
            @PathVariable Long id,
            @RequestParam Double quantity
    ) {
        log.info("Restocking fuel inventory ID={} with quantity={}", id, quantity);
        FuelInventory restockedInventory = fuelInventoryService.restockFuel(id, quantity);
        log.info("Fuel inventory restocked ID={} newQuantity={}", id, restockedInventory.getAvailableQuantity());
        return ResponseEntity.ok(restockedInventory);
    }

    // ------------------------------------------------------------------------
    // Check low stock
    // ------------------------------------------------------------------------
    @GetMapping("/low-stock/{id}")
    @Operation(
            summary = "Low Stock Alert",
            description = "Check if the fuel inventory is below the defined threshold",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Low stock status returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "404", description = "Fuel inventory not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<Boolean> checkLowStock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") Double threshold
    ) {
        log.info("Checking low stock for inventory ID={} with threshold={}", id, threshold);
        boolean isLow = fuelInventoryService.isLowStock(id, threshold);
        return ResponseEntity.ok(isLow);
    }
}
