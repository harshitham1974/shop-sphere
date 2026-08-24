package com.ecom.shopsphere.exception;

import com.ecom.shopsphere.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.error("Email Already Exists: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message("Registration Failed")
                .data(Map.of("email", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        log.error("Login failed: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Login Failed")
                .data(Map.of("credentials", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse<Map<String, String>> errorResponse =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Validation Failed")
                        .path(request.getRequestURI())
                        .data(errors)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        log.error("User not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("User Not Found")
                .data(Map.of("user", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(PasswordChangeFailedException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handlePasswordChangeFailed(
            PasswordChangeFailedException ex,
            HttpServletRequest request) {

        log.error("Password Change failed: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Password Change Failed")
                .data(Map.of("password", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request) {

        log.error("Product not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Product Not Found")
                        .path(request.getRequestURI())
                        .data(Map.of("product", ex.getMessage()))
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleCategoryAlreadyExists(
            CategoryAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message("Category Creation Failed")
                        .path(request.getRequestURI())
                        .data(Map.of("category", ex.getMessage()))
                        .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleCategoryNotFound(
            CategoryNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Category Not Found")
                        .path(request.getRequestURI())
                        .data(Map.of("category", ex.getMessage()))
                        .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleCartNotFound(
            CartNotFoundException ex,
            HttpServletRequest request) {

        log.error("Cart not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("Cart Not Found")
                .data(Map.of("cart", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleCartItemNotFound(
            CartItemNotFoundException ex,
            HttpServletRequest request) {

        log.error("Cart item not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error = ErrorResponse.<Map<String, String>>builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("Cart Item Not Found")
                .data(Map.of("cartItem", ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(WishlistNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleWishlistNotFound(
            WishlistNotFoundException ex,
            HttpServletRequest request) {

        log.error("Wishlist not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Wishlist Not Found")
                        .data(Map.of("wishlist", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(WishlistItemNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleWishlistItemNotFound(
            WishlistItemNotFoundException ex,
            HttpServletRequest request) {

        log.error("Wishlist item not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Wishlist Item Not Found")
                        .data(Map.of("wishlistItem", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleAddressNotFound(
            AddressNotFoundException ex,
            HttpServletRequest request) {

        log.error("Address not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Address Not Found")
                        .data(Map.of("address", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {


        log.error("Order not found: {}", ex.getMessage());


        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Order Not Found")
                        .data(Map.of("order", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();


        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(OrderCancellationFailedException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleOrderCancellationFailed(
            OrderCancellationFailedException ex,
            HttpServletRequest request) {


        log.error("Order cancellation failed: {}", ex.getMessage());


        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Order Cancellation Failed")
                        .data(Map.of("order", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();


        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleCartEmpty(
            CartEmptyException ex,
            HttpServletRequest request) {

        log.error("Cart empty: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Cart Empty")
                        .data(Map.of("cart", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handlePaymentFailed(
            PaymentFailedException ex,
            HttpServletRequest request) {

        log.error("Payment failed: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Payment Failed")
                        .data(Map.of("payment", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        log.error("Payment not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Payment Not Found")
                        .data(Map.of("payment", ex.getMessage()))
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse<Map<String,String>>>
    handleInvalidOrderStatus(
            InvalidOrderStatusException ex,
            HttpServletRequest request) {

        log.error(
                "Invalid order status: {}",
                ex.getMessage());

        ErrorResponse<Map<String,String>> error =
                ErrorResponse.<Map<String,String>>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Invalid Order Status")
                        .data(
                                Map.of(
                                        "order",
                                        ex.getMessage()
                                )
                        )
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleReviewNotFound(
            ReviewNotFoundException ex,
            HttpServletRequest request) {

        log.error("Review not found: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Review Not Found")
                        .path(request.getRequestURI())
                        .data(Map.of("review", ex.getMessage()))
                        .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleReviewAlreadyExists(
            ReviewAlreadyExistsException ex,
            HttpServletRequest request) {

        log.error("Review already exists: {}", ex.getMessage());

        ErrorResponse<Map<String, String>> error =
                ErrorResponse.<Map<String, String>>builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message("Review Already Exists")
                        .path(request.getRequestURI())
                        .data(Map.of("review", ex.getMessage()))
                        .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}