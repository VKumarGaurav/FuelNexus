package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.ProductDTO;
import com.fuel.nexus.entity.Product;
import com.fuel.nexus.exception.exceptions.OutOfStockException;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.ProductRepository;
import com.fuel.nexus.service.services.ProductService;
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
@Tag(name = "Product Service", description = "Service implementation for managing fuel products (Gas & Liquid)")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String PRODUCT_TOPIC = "product-topic";

    /**
     * Save a new product
     */
    @Override
    @Operation(summary = "Save Product", description = "Create and persist a new product (Gas or Liquid Fuel)")
    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(ProductDTO productDTO) {
        log.info("Saving new product: {}", productDTO.getName());

        Product product = modelMapper.map(productDTO, Product.class);
        Product savedProduct = productRepository.save(product);

        log.info("Product saved with ID={}", savedProduct.getId());

        // Kafka notification
        kafkaTemplate.send(PRODUCT_TOPIC, "New product added: " + savedProduct.getName());

        return savedProduct;
    }

    /**
     * Get all products with pagination
     */
    @Override
    @Operation(summary = "Get Products (Paged)", description = "Fetch all products with pagination support")
    @Cacheable(value = "productsPage", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Fetching products page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable);
    }

    /**
     * Get product by ID
     */
    @Override
    @Operation(summary = "Get Product by ID", description = "Retrieve a product by its unique identifier")
    @Cacheable(value = "products", key = "#productId")
    public Optional<Product> getProductById(Long productId) {
        log.info("Fetching product by ID={}", productId);
        return productRepository.findById(productId);
    }

    /**
     * Update product details
     */
    @Override
    @Operation(summary = "Update Product", description = "Update product details (price, type, unit, etc.)")
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long productId, ProductDTO productDTO) {
        log.info("Updating product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Update fields
        product.setName(productDTO.getName());
        product.setFuelType(productDTO.getFuelType());
        product.setUnit(productDTO.getUnit());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated ID={} name={}", updatedProduct.getId(), updatedProduct.getName());

        // Kafka notification
        kafkaTemplate.send(PRODUCT_TOPIC, "Product updated: " + updatedProduct.getName());

        return updatedProduct;
    }

    /**
     * Delete product by ID
     */
    @Override
    @Operation(summary = "Delete Product", description = "Delete a product from the system by ID")
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        log.info("Deleting product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        productRepository.delete(product);

        log.info("Product deleted ID={}", productId);

        // Kafka notification
        kafkaTemplate.send(PRODUCT_TOPIC, "Product deleted: " + product.getName());
    }

    /**
     * Find product by name
     */
    @Override
    @Operation(summary = "Find Product by Name", description = "Search a product using its name")
    public Optional<Product> findByName(String name) {
        log.info("Searching product by name={}", name);
        return productRepository.findByName(name);
    }

    /**
     * Track product stock levels
     */
    @Override
    @Operation(summary = "Track Product Stock", description = "Check current stock levels for a given product")
    public Long getStockLevel(Long productId) {
        log.info("Getting stock level for product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return product.getStockQuantity();
    }

    /**
     * Handle low stock alerts
     */
    @Override
    @Operation(summary = "Low Stock Alert", description = "Check if a product's stock level is below threshold and trigger alert")
    public boolean isLowStock(Long productId, Double threshold) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        boolean lowStock = product.getStockQuantity() < threshold;
        if (lowStock) {
            log.warn("Low stock detected for product ID={} stock={}", productId, product.getStockQuantity());
            kafkaTemplate.send(PRODUCT_TOPIC, "Low stock alert for product: " + product.getName());
        }
        return lowStock;
    }
}
