package com.fuel.nexus.service.services;

import com.fuel.nexus.dto.ProductDTO;
import com.fuel.nexus.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


@Tag(name = "Product Service", description = "Service interface for managing fuel products (Gas & Liquid)")
public interface ProductService {

    // Save a new product
    @Operation(summary = "Save Product", description = "Create and persist a new product (Gas or Liquid Fuel)")
    Product saveProduct(ProductDTO productDTO);

    // Get all products paged
    @Operation(summary = "Get Products (Paged)", description = "Fetch all products with pagination support")
    Page<Product> getAllProducts(Pageable pageable);

    // Get product by ID
    @Operation(summary = "Get Product by ID", description = "Retrieve a product by its unique identifier")
    @Cacheable(value = "products", key = "#id")
    Optional<Product> getProductById(Long productId);

    // Update product
    @Operation(summary = "Update Product", description = "Update product details (price, type, unit, etc.)")
    Product updateProduct(Long productId, ProductDTO productDTO);

    // Delete product by ID
    @Operation(summary = "Delete Product", description = "Delete a product from the system by ID")
    @CacheEvict(value = "products", key = "#id")
    void deleteProduct(Long productId);

    // Find product by name
    @Operation(summary = "Find Product by Name", description = "Search a product using its name")
    Optional<Product> findByName(String name);

    // Track product stock levels
    @Operation(summary = "Track Product Stock", description = "Check current stock levels for a given product")
    Long getStockLevel(Long productId);

    // Handle low stock alerts for a product
    @Operation(summary = "Low Stock Alert", description = "Check if a product's stock level is below threshold and trigger alert")
    boolean isLowStock(Long productId, Double threshold);
}


