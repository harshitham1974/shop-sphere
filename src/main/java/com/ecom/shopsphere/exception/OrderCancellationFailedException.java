package com.ecom.shopsphere.exception;

public class OrderCancellationFailedException extends RuntimeException {
    public OrderCancellationFailedException(String message) {
        super(message);
    }
}
