package com.fuel.nexus.exception.exceptions;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String productName) {
        super("Product '" + productName + "' is out of stock");
    }
}