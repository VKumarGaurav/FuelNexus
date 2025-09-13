package com.fuel.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_kyc")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer ID is required")
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @NotBlank(message = "Aadhar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid Aadhar number format")
    @Column(nullable = false, unique = true, length = 12)
    private String aadharNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN number format")
    @Column(nullable = false, unique = true, length = 10)
    private String panNumber;

    @PastOrPresent(message = "Verification date cannot be in the future")
    @Column(nullable = false)
    private LocalDate verificationDate = LocalDate.now();
}
