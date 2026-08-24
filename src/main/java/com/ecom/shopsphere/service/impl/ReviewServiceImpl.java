package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteReviewResponseDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.Review;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.ReviewAlreadyExistsException;
import com.ecom.shopsphere.exception.ReviewNotFoundException;
import com.ecom.shopsphere.mapper.ReviewMapper;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.ReviewRepository;
import com.ecom.shopsphere.service.CurrentUserService;
import com.ecom.shopsphere.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ReviewResponseDTO createReview(CreateReviewRequestDTO request) {
        log.info("Starting review creation.");

        User user = currentUserService.getCurrentUser();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + request.getProductId() + " does not exist."));

        if (reviewRepository.existsByUserUserIdAndProductProductId(
                user.getUserId(), product.getProductId())) {
            throw new ReviewAlreadyExistsException(
                    "You have already reviewed this product.");
        }

        Review review = reviewMapper.toEntity(request, user, product);
        Review savedReview = reviewRepository.save(review);

        log.info(
                "Review created successfully. Review ID: {}, Product ID: {}",
                savedReview.getReviewId(),
                savedReview.getProduct().getProductId()
        );

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public ReviewResponseDTO getReviewById(Long reviewId) {
        log.info("Fetching review with ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("Review not found with ID: {}", reviewId);
                    return new ReviewNotFoundException(
                            "Review with ID " + reviewId + " does not exist.");
                });

        log.info("Review fetched successfully. Review ID: {}", reviewId);
        return reviewMapper.toResponse(review);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByProductId(Long productId) {
        log.info("Fetching reviews for product ID: {}", productId);

        List<Review> reviews = reviewRepository.findByProductProductId(productId);

        log.info("Total reviews found for product {}: {}", productId, reviews.size());

        return reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByCurrentUser() {
        log.info("Fetching reviews for current user.");

        User user = currentUserService.getCurrentUser();
        List<Review> reviews = reviewRepository.findByUserUserId(user.getUserId());

        log.info("Total reviews found for user {}: {}", user.getUserId(), reviews.size());

        return reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewResponseDTO updateReview(Long reviewId, UpdateReviewRequestDTO request) {
        log.info("Updating review with ID: {}", reviewId);

        User currentUser = currentUserService.getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("Review not found with ID: {}", reviewId);
                    return new ReviewNotFoundException(
                            "Review with ID " + reviewId + " does not exist.");
                });

        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            log.error("User {} is not authorized to update review {}",
                    currentUser.getUserId(), reviewId);
            throw new ReviewNotFoundException(
                    "You are not authorized to update this review.");
        }

        reviewMapper.updateReviewFromRequest(request, review);
        Review updatedReview = reviewRepository.save(review);

        log.info("Review updated successfully. Review ID: {}", reviewId);
        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    public DeleteReviewResponseDTO deleteReview(Long reviewId) {
        log.info("Deleting review with ID: {}", reviewId);

        User currentUser = currentUserService.getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("Review not found with ID: {}", reviewId);
                    return new ReviewNotFoundException(
                            "Review with ID " + reviewId + " does not exist.");
                });

        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            log.error("User {} is not authorized to delete review {}",
                    currentUser.getUserId(), reviewId);
            throw new ReviewNotFoundException(
                    "You are not authorized to delete this review.");
        }

        DeleteReviewResponseDTO response = DeleteReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .state("DELETED")
                .build();

        reviewRepository.delete(review);

        log.info("Review deleted successfully. Review ID: {}", reviewId);
        return response;
    }
}
