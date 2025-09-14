package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.CustomerRequestDTO;
import com.fuel.nexus.dto.CustomerResponseDTO;
import com.fuel.nexus.entity.Customer;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.CustomerRepository;
import com.fuel.nexus.service.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Tag(name = "Customer Management", description = "Customer profile operations")
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String CUSTOMER_TOPIC = "customer-topic";

    /**
     * Create a new customer
     */
    @Override
    @Operation(summary = "Create a new customer", description = "Registers a new customer with profile information")
    @CacheEvict(value = {"allCustomers", "customer"}, allEntries = true) // Evict cache after creation
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        log.info("Creating new customer with email={}", dto.getEmail());

        // Map DTO to entity
        Customer customer = modelMapper.map(dto, Customer.class);

        // Save to DB
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with id={}", savedCustomer.getId());

        // Kafka notification
        kafkaTemplate.send(CUSTOMER_TOPIC, "New customer created with id: " + savedCustomer.getId());
        log.debug("Kafka message sent for new customer id={}", savedCustomer.getId());

        // Map back to DTO
        return modelMapper.map(savedCustomer, CustomerResponseDTO.class);
    }

    /**
     * Update existing customer by ID
     */
    @Override
    @Operation(summary = "Update customer", description = "Updates an existing customer's profile details")
    @CacheEvict(value = {"allCustomers", "customer"}, allEntries = true) // Evict cache after update
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
        log.info("Updating customer with id={}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Update fields
        existingCustomer.setFullName(dto.getFullName());
        existingCustomer.setEmail(dto.getEmail());
        existingCustomer.setMobileNumber(dto.getMobileNumber());
        existingCustomer.setAddress(dto.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated with id={}", updatedCustomer.getId());

        // Kafka notification
        kafkaTemplate.send(CUSTOMER_TOPIC, "Customer updated with id: " + updatedCustomer.getId());
        log.debug("Kafka message sent for updated customer id={}", updatedCustomer.getId());

        return modelMapper.map(updatedCustomer, CustomerResponseDTO.class);
    }

    /**
     * Fetch customer by ID
     */
    @Override
    @Operation(summary = "Get customer by ID", description = "Fetches a customer's profile by their ID")
    @Cacheable(value = "customer", key = "#id") // Cache per customer
    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Fetching customer with id={}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        return modelMapper.map(customer, CustomerResponseDTO.class);
    }

    /**
     * Fetch all customers
     */
    @Override
    @Operation(summary = "Get all customers", description = "Retrieves a list of all registered customers")
    @Cacheable(value = "allCustomers") // Cache list of all customers
    public List<CustomerResponseDTO> getAllCustomers() {
        log.info("Fetching all customers");

        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(c -> modelMapper.map(c, CustomerResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Delete customer by ID
     */
    @Override
    @Operation(summary = "Delete customer", description = "Deletes a customer by their ID")
    @CacheEvict(value = {"allCustomers", "customer"}, allEntries = true) // Evict caches after deletion
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id={}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customerRepository.delete(customer);
        log.info("Customer deleted with id={}", id);

        // Kafka notification
        kafkaTemplate.send(CUSTOMER_TOPIC, "Customer deleted with id: " + id);
        log.debug("Kafka message sent for deleted customer id={}", id);
    }
}
