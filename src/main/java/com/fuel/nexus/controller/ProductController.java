package com.fuel.nexus.controller;

import com.fuel.nexus.dto.ProductDTO;
import com.fuel.nexus.entity.Product;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.service.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.requests.ApiError;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Controller", description = "REST APIs for managing fuel products (Gas & Liquid)")
public class ProductController {

    private final ProductService productService;

    /**
     * Save a new product
     */
    @PostMapping
    @Operation(summary = "Save Product", description = "Create and persist a new product (Gas or Liquid Fuel)")
    @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Product> saveProduct(@RequestBody ProductDTO productDTO) {
        log.info("API Request: Save product {}", productDTO.getName());
        Product saved = productService.saveProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID", description = "Retrieve a product by its unique identifier")
    @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("API Request: Fetch product ID={}", id);
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Product", description = "Update product details (price, type, unit, etc.)")
    @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        log.info("API Request: Update product ID={}", id);
        Product updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Product", description = "Delete a product from the system by ID")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("API Request: Delete product ID={}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Find by name
     */
    @GetMapping("/search")
    @Operation(summary = "Find Product by Name", description = "Search a product using its name")
    @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Product> findByName(@RequestParam String name) {
        log.info("API Request: Search product by name={}", name);
        return productService.findByName(name)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + name));
    }

    /**
     * Track stock level
     */
    @GetMapping("/{id}/stock")
    @Operation(summary = "Track Product Stock", description = "Check current stock levels for a given product")
    @ApiResponse(responseCode = "200", description = "Stock level retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Long> getStockLevel(@PathVariable Long id) {
        log.info("API Request: Get stock for product ID={}", id);
        Long stock = productService.getStockLevel(id);
        return ResponseEntity.ok(stock);
    }

    /**
     * Low stock check
     */
    @GetMapping("/{id}/low-stock")
    @Operation(summary = "Low Stock Alert", description = "Check if a product's stock level is below threshold")
    @ApiResponse(responseCode = "200", description = "Low stock status retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Boolean> isLowStock(@PathVariable Long id,
                                              @RequestParam(defaultValue = "50") Double threshold) {
        log.info("API Request: Low stock check for product ID={} with threshold={}", id, threshold);
        boolean result = productService.isLowStock(id, threshold);
        return ResponseEntity.ok(result);
    }
}
