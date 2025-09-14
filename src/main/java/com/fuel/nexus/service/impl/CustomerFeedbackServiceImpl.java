package com.fuel.nexus.service.impl;

import com.fuel.nexus.dto.CustomerFeedbackDTO;
import com.fuel.nexus.entity.CustomerFeedback;
import com.fuel.nexus.exception.exceptions.ResourceNotFoundException;
import com.fuel.nexus.repository.CustomerFeedbackRepository;
import com.fuel.nexus.service.services.CustomerFeedbackService;
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
@Tag(name = "Customer Feedback Management", description = "APIs for managing customer complaints and feedback")
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    private final CustomerFeedbackRepository feedbackRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String FEEDBACK_TOPIC = "customer-feedback-topic";

    /**
     * Submit a new customer feedback.
     */
    @Override
    @Operation(
            summary = "Submit Customer Feedback",
            description = "Stores a customer's feedback or complaint message"
    )
    @CacheEvict(value = "allFeedback", allEntries = true) // Evict cache to refresh list
    public CustomerFeedbackDTO submitFeedback(CustomerFeedbackDTO feedbackDTO) {
        log.info("Submitting new feedback for customerId={}", feedbackDTO.getCustomerId());

        // Map DTO to entity
        CustomerFeedback feedback = modelMapper.map(feedbackDTO, CustomerFeedback.class);

        // Save feedback to DB
        CustomerFeedback savedFeedback = feedbackRepository.save(feedback);

        log.info("Feedback saved with id={}", savedFeedback.getId());

        // Publish notification to Kafka topic
        kafkaTemplate.send(FEEDBACK_TOPIC, "New feedback submitted with id: " + savedFeedback.getId());
        log.debug("Kafka message sent for feedback id={}", savedFeedback.getId());

        // Map entity back to DTO and return
        return modelMapper.map(savedFeedback, CustomerFeedbackDTO.class);
    }

    /**
     * Get feedbacks submitted by a specific customer.
     */
    @Override
    @Operation(
            summary = "Get Feedback by Customer ID",
            description = "Fetches all feedback/complaints submitted by a given customer"
    )
    @Cacheable(value = "customerFeedback", key = "#customerId") // Cache per customerId
    public List<CustomerFeedbackDTO> getFeedbackByCustomerId(Long customerId) {
        log.info("Fetching feedback for customerId={}", customerId);

        List<CustomerFeedback> feedbackList = feedbackRepository.findByCustomerId(customerId);

        if (feedbackList.isEmpty()) {
            log.warn("No feedback found for customerId={}", customerId);
            throw new ResourceNotFoundException("No feedback found for customerId: " + customerId);
        }

        return feedbackList.stream()
                .map(fb -> modelMapper.map(fb, CustomerFeedbackDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Get all customer feedback entries.
     */
    @Override
    @Operation(
            summary = "Get All Feedback Records",
            description = "Retrieves all feedback entries from customers"
    )
    @Cacheable(value = "allFeedback") // Cache all feedback list
    public List<CustomerFeedbackDTO> getAllFeedback() {
        log.info("Fetching all customer feedback records");

        List<CustomerFeedback> feedbackList = feedbackRepository.findAll();

        return feedbackList.stream()
                .map(fb -> modelMapper.map(fb, CustomerFeedbackDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Delete a feedback entry by ID.
     */
    @Override
    @Operation(
            summary = "Delete Customer Feedback",
            description = "Deletes a specific feedback/complaint entry by its ID"
    )
    @CacheEvict(value = {"allFeedback", "customerFeedback"}, allEntries = true) // Evict caches
    public void deleteFeedback(Long feedbackId) {
        log.info("Deleting feedback with id={}", feedbackId);

        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));

        feedbackRepository.delete(feedback);

        log.info("Feedback deleted with id={}", feedbackId);

        // Optionally, publish deletion event to Kafka
        kafkaTemplate.send(FEEDBACK_TOPIC, "Feedback deleted with id: " + feedbackId);
        log.debug("Kafka message sent for deleted feedback id={}", feedbackId);
    }
}
