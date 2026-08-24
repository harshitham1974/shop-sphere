package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteReviewResponseDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO createReview(CreateReviewRequestDTO request);

    ReviewResponseDTO getReviewById(Long reviewId);

    List<ReviewResponseDTO> getReviewsByProductId(Long productId);

    List<ReviewResponseDTO> getReviewsByCurrentUser();

    ReviewResponseDTO updateReview(Long reviewId, UpdateReviewRequestDTO request);

    DeleteReviewResponseDTO deleteReview(Long reviewId);
}
