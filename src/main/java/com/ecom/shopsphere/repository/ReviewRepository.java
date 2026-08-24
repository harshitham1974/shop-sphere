package com.ecom.shopsphere.repository;

import com.ecom.shopsphere.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductProductId(Long productId);

    List<Review> findByUserUserId(Long userId);

    Optional<Review> findByUserUserIdAndProductProductId(Long userId, Long productId);

    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);
}
