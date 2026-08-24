package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.Review;
import com.ecom.shopsphere.entity.User;

public interface ReviewMapper {

    Review toEntity(CreateReviewRequestDTO request, User user, Product product);

    ReviewResponseDTO toResponse(Review review);

    void updateReviewFromRequest(UpdateReviewRequestDTO request, Review review);
}
