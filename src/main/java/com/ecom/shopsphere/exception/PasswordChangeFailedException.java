package com.ecom.shopsphere.exception;

public class PasswordChangeFailedException extends RuntimeException {
    public PasswordChangeFailedException(String message) {
        super(message);
    }
}
