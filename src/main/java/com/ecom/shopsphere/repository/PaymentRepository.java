package com.ecom.shopsphere.repository;

import com.ecom.shopsphere.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {


    Optional<Payment> findByOrderOrderId(Long orderId);


    Optional<Payment> findByTransactionId(String transactionId);

}