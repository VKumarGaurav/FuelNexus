package com.fuel.nexus.dto;

import com.fuel.nexus.utility.FuelType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDTO {

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Product type is required")
    @Pattern(regexp = "Gas|Liquid", message = "Product type must be either 'Gas' or 'Liquid'")
    private String type;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Unit cannot be null")
    @Pattern(regexp = "Litre|Kg|Gallon", message = "Unit must be Litre, Kg, or Gallon")
    private String unit;

    @NotBlank(message = "Manufacturer email is required")
    @Email(message = "Manufacturer email must be valid")
    private String manufacturerEmail;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotNull(message = "Stock Quant is required")
    private Long stockQuantity;}

