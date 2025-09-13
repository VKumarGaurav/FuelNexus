package com.fuel.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer ID is required")
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    @Column(nullable = false, length = 100)
    private String password;

    @PastOrPresent(message = "Created date cannot be in the future")
    @Column(nullable = false, updatable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(nullable = false)
    private boolean active = true;
}

