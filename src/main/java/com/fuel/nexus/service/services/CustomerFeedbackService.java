package com.fuel.nexus.service.services;

import com.fuel.nexus.dto.CustomerFeedbackDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Tag(name = "Customer Feedback Management", description = "APIs for managing customer complaints and feedback")
public interface CustomerFeedbackService {

    /**
     * Submit a new customer feedback/complaint.
     */
    @Operation(
            summary = "Submit Customer Feedback",
            description = "Stores a customer's feedback or complaint message",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid feedback request")
            }
    )
    CustomerFeedbackDTO submitFeedback(CustomerFeedbackDTO feedbackDTO);

    /**
     * Get feedbacks submitted by a customer.
     */
    @Operation(
            summary = "Get Feedback by Customer ID",
            description = "Fetches all feedback/complaints submitted by a given customer",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found or no feedback available")
            }
    )
    List<CustomerFeedbackDTO> getFeedbackByCustomerId(Long customerId);

    /**
     * Get all customer feedback records.
     */
    @Operation(
            summary = "Get All Feedback Records",
            description = "Retrieves all feedback entries from customers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of feedback records retrieved successfully")
            }
    )
    List<CustomerFeedbackDTO> getAllFeedback();

    /**
     * Delete a feedback entry by ID.
     */
    @Operation(
            summary = "Delete Customer Feedback",
            description = "Deletes a specific feedback/complaint entry by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feedback deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Feedback not found")
            }
    )
    void deleteFeedback(Long feedbackId);
}

