package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.CustomerKycDTO;
import com.fuel.nexus.entity.CustomerKyc;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.CustomerKycRepository;
import com.fuel.nexus.service.services.CustomerKycService;
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
@Tag(name = "Customer KYC Management", description = "APIs for managing customer identity proofs (KYC)")
public class CustomerKycServiceImpl implements CustomerKycService {

    private final CustomerKycRepository kycRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String KYC_TOPIC = "customer-kyc-topic";

    /**
     * Save KYC details for a customer.
     */
    @Override
    @Operation(summary = "Save Customer KYC", description = "Stores customer KYC information such as Aadhaar and PAN details")
    @CacheEvict(value = {"allKycRecords", "customerKyc"}, allEntries = true) // Evict cache after save
    public CustomerKycDTO saveCustomerKyc(CustomerKycDTO customerKycDTO) {
        log.info("Saving KYC for customerId={}", customerKycDTO.getCustomerId());

        // Map DTO to entity
        CustomerKyc kyc = modelMapper.map(customerKycDTO, CustomerKyc.class);

        // Save KYC to DB
        CustomerKyc savedKyc = kycRepository.save(kyc);
        log.info("KYC saved with id={}", savedKyc.getId());

        // Send Kafka notification
        kafkaTemplate.send(KYC_TOPIC, "KYC saved for customerId: " + savedKyc.getId());
        log.debug("Kafka message sent for saved KYC id={}", savedKyc.getId());

        // Map entity back to DTO
        return modelMapper.map(savedKyc, CustomerKycDTO.class);
    }

    /**
     * Get KYC details of a customer by ID.
     */
    @Override
    @Operation(summary = "Get Customer KYC by Customer ID", description = "Fetches the KYC details for a given customer ID")
    @Cacheable(value = "customerKyc", key = "#customerId") // Cache per customerId
    public CustomerKycDTO getKycByCustomerId(Long customerId) {
        log.info("Fetching KYC for customerId={}", customerId);

        CustomerKyc kyc = (CustomerKyc) kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found for customerId: " + customerId));

        return modelMapper.map(kyc, CustomerKycDTO.class);
    }

    /**
     * Get all KYC records.
     */
    @Override
    @Operation(summary = "Get All Customer KYC Records", description = "Retrieves all customer KYC entries")
    @Cacheable(value = "allKycRecords") // Cache all KYC records
    public List<CustomerKycDTO> getAllKycRecords() {
        log.info("Fetching all KYC records");

        List<CustomerKyc> kycList = kycRepository.findAll();

        return kycList.stream()
                .map(kyc -> modelMapper.map(kyc, CustomerKycDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Delete a customer's KYC record.
     */
    @Override
    @Operation(summary = "Delete Customer KYC", description = "Deletes the KYC record for a given customer ID")
    @CacheEvict(value = {"allKycRecords", "customerKyc"}, allEntries = true) // Evict caches after delete
    public void deleteKyc(Long customerId) {
        log.info("Deleting KYC for customerId={}", customerId);

        CustomerKyc kyc = (CustomerKyc) kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found for customerId: " + customerId));

        kycRepository.delete(kyc);
        log.info("KYC deleted for customerId={}", customerId);

        // Kafka notification
        kafkaTemplate.send(KYC_TOPIC, "KYC deleted for customerId: " + customerId);
        log.debug("Kafka message sent for deleted KYC customerId={}", customerId);
    }
}
