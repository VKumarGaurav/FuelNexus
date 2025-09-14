package com.fuel.nexus.exception.advisor;

import com.fuel.nexus.exception.entity.ApiErrorResponse;
import com.fuel.nexus.exception.exceptions.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@Tag(
        name = "Global Exception Handling",
        description = "Centralized exception handling for all REST APIs. Returns standardized error responses."
)
public class GlobalExceptionHandler {

    // Handle generic runtime exceptions
    @Operation(
            summary = "Handles FuelNexus runtime errors",
            description = "Catches application-specific runtime errors and maps them to HTTP 400",
            responses = {
                    @ApiResponse(responseCode = "400", description = "Bad request - application runtime error")
            }
    )
    @ExceptionHandler(FuelNexusRuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleFuelNexusRuntime(
            FuelNexusRuntimeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    // Handle resource not found
    @Operation(
            summary = "Handles resource not found errors",
            description = "Catches ResourceNotFoundException and maps it to HTTP 404",
            responses = {
                    @ApiResponse(responseCode = "404", description = "Resource not found")
            }
    )
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    // Handle unauthorized access
    @Operation(
            summary = "Handles unauthorized access errors",
            description = "Catches NotAuthorizedException and maps it to HTTP 401",
            responses = {
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            NotAuthorizedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }

    // Handle out of stock
    @Operation(
            summary = "Handles out of stock errors",
            description = "Catches OutOfStockException and maps it to HTTP 409 (conflict)",
            responses = {
                    @ApiResponse(responseCode = "409", description = "Out of stock conflict")
            }
    )
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiErrorResponse> handleOutOfStock(
            OutOfStockException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    // Handle duplicate username
    @Operation(
            summary = "Handles duplicate username errors",
            description = "Catches DuplicateUsernameException and maps it to HTTP 409 (conflict)",
            responses = {
                    @ApiResponse(responseCode = "409", description = "Duplicate username conflict")
            }
    )
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }


    // -------------------------
    // Handle BookingNotFound
    // -------------------------
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotFound(BookingNotFoundException ex,
                                                               HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // -------------------------
    // Handle OrderNotFound
    // -------------------------
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException ex,
                                                             HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // -------------------------
    // Handle ProductNotFound
    // -------------------------
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(ProductNotFoundException ex,
                                                               HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // -------------------------
    // Handle Out of Stock
    // -------------------------
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ApiErrorResponse> handleOutOfStock(ProductOutOfStockException ex,
                                                          HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // -------------------------
    // Handle CustomerNotFound
    // -------------------------
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex,
                                                                HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }


    // -------------------------
    // Handle Constraint Violations (e.g. @Pattern, @Size)
    // -------------------------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Constraint violation");

        return buildErrorResponse(new RuntimeException(message), HttpStatus.BAD_REQUEST, request);
    }

    // -------------------------
    // Generic Exception Fallback
    // -------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // -------------------------
    // Helper to build ApiErrorResponse
    // -------------------------
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(Exception ex,
                                                             HttpStatus status,
                                                             HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .time(LocalDateTime.now())
                .statusCode(status.value())
                .message(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    // Helper method to construct standardized error responses
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception ex,
            HttpServletRequest request,
            HttpStatus status
    ) {
        // Extract class and line number from stack trace
        String className = "N/A";
        String lineNumber = "N/A";
        if (ex.getStackTrace().length > 0) {
            className = ex.getStackTrace()[0].getClassName();
            lineNumber = String.valueOf(ex.getStackTrace()[0].getLineNumber());
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .path(request.getRequestURI())
                .time(LocalDateTime.now())
                .message(ex.getMessage())
                .api(request.getMethod())
                .statusCode(status.value())
                .className(className)
                .lineNumber(lineNumber)
                .build();

        log.error("API Error [{} {}] -> {} ({}:{})", request.getMethod(), request.getRequestURI(),
                ex.getMessage(), className, lineNumber);

        return new ResponseEntity<>(response, status);
    }

    // ------------------------------------------------------------------------
    // Handle Billing Not Found Exception
    // ------------------------------------------------------------------------
    @ExceptionHandler(BillingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBillingNotFound(BillingNotFoundException ex) {
        log.error("BillingNotFoundException: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Billing Not Found");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------
    // Handle Delivery Not Found Exception
    // ------------------------------------------------------------------------
    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDeliveryNotFound(DeliveryNotFoundException ex) {
        log.error("DeliveryNotFoundException: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Delivery Not Found");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------
    // Generic Exception Handler (Fallback)
    // ------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        log.error("Unexpected Exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Handle PaymentNotFoundException
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(PaymentNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Payment Not Found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
