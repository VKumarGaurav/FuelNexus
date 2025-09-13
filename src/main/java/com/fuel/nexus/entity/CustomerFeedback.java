package com.fuel.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_feedback")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer ID is required")
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank(message = "Feedback message is required")
    @Size(max = 500, message = "Feedback must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String message;

    @PastOrPresent(message = "Feedback date cannot be in the future")
    @Column(nullable = false)
    private LocalDate feedbackDate = LocalDate.now();
}
