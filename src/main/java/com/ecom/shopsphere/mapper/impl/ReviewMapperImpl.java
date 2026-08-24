package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.Review;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.mapper.ReviewMapper;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toEntity(CreateReviewRequestDTO request, User user, Product product) {
        return Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .product(product)
                .build();
    }

    @Override
    public ReviewResponseDTO toResponse(Review review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .userId(review.getUser().getUserId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getProductId())
                .productName(review.getProduct().getProductName())
                .build();
    }

    @Override
    public void updateReviewFromRequest(UpdateReviewRequestDTO request, Review review) {
        review.setRating(request.getRating());
        review.setComment(request.getComment());
    }
}
