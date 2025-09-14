package com.fuel.nexus.controller;

import com.fuel.nexus.dto.CustomerFeedbackDTO;
import com.fuel.nexus.service.services.CustomerFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer Feedback Management
 * Handles submission, retrieval, and deletion of customer complaints/feedback
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-feedback")
@RequiredArgsConstructor
@Tag(name = "Customer Feedback Controller", description = "APIs for submitting, retrieving, and deleting customer feedback")
public class CustomerFeedbackController {

    private final CustomerFeedbackService feedbackService;

    // ------------------------------------------------------------------------
    // Endpoint to submit new customer feedback
    // ------------------------------------------------------------------------
    @PostMapping("/submit")
    @Operation(
            summary = "Submit Customer Feedback",
            description = "Stores a customer's feedback or complaint message",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid feedback request")
            }
    )
    public ResponseEntity<CustomerFeedbackDTO> submitFeedback(@Valid @RequestBody CustomerFeedbackDTO feedbackDTO) {
        log.info("Received new feedback submission for customerId={}", feedbackDTO.getCustomerId());
        CustomerFeedbackDTO savedFeedback = feedbackService.submitFeedback(feedbackDTO);
        log.info("Feedback saved successfully with id={}", savedFeedback.getId());
        return ResponseEntity.ok(savedFeedback);
    }

    // ------------------------------------------------------------------------
    // Endpoint to get feedback by customer ID
    // ------------------------------------------------------------------------
    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Get Feedback by Customer ID",
            description = "Fetches all feedback/complaints submitted by a given customer",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found or no feedback available")
            }
    )
    public ResponseEntity<List<CustomerFeedbackDTO>> getFeedbackByCustomerId(@PathVariable Long customerId) {
        log.info("Fetching feedback for customerId={}", customerId);
        List<CustomerFeedbackDTO> feedbackList = feedbackService.getFeedbackByCustomerId(customerId);
        log.info("Retrieved {} feedback entries for customerId={}", feedbackList.size(), customerId);
        return ResponseEntity.ok(feedbackList);
    }

    // ------------------------------------------------------------------------
    // Endpoint to get all feedback records
    // ------------------------------------------------------------------------
    @GetMapping("/all")
    @Operation(
            summary = "Get All Feedback Records",
            description = "Retrieves all feedback entries from customers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of feedback records retrieved successfully")
            }
    )
    public ResponseEntity<List<CustomerFeedbackDTO>> getAllFeedback() {
        log.info("Fetching all feedback records");
        List<CustomerFeedbackDTO> allFeedback = feedbackService.getAllFeedback();
        log.info("Retrieved {} feedback records", allFeedback.size());
        return ResponseEntity.ok(allFeedback);
    }

    // ------------------------------------------------------------------------
    // Endpoint to delete feedback by ID
    // ------------------------------------------------------------------------
    @DeleteMapping("/delete/{feedbackId}")
    @Operation(
            summary = "Delete Customer Feedback",
            description = "Deletes a specific feedback/complaint entry by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Feedback not found")
            }
    )
    public ResponseEntity<String> deleteFeedback(@PathVariable Long feedbackId) {
        log.info("Received request to delete feedback with id={}", feedbackId);
        feedbackService.deleteFeedback(feedbackId);
        log.info("Feedback deleted successfully with id={}", feedbackId);
        return ResponseEntity.ok("Feedback deleted successfully with id: " + feedbackId);
    }
}

