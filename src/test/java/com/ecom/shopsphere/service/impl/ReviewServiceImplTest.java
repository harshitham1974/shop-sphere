package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteReviewResponseDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.entity.Category;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.Review;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.ReviewAlreadyExistsException;
import com.ecom.shopsphere.exception.ReviewNotFoundException;
import com.ecom.shopsphere.mapper.ReviewMapper;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.ReviewRepository;
import com.ecom.shopsphere.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User createTestUser(Long id) {
        return User.builder()
                .userId(id)
                .email("user" + id + "@test.com")
                .fullName("Test User " + id)
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    private Product createTestProduct(Long id) {
        Category category = Category.builder()
                .categoryId(1L)
                .categoryName("Electronics")
                .build();
        return Product.builder()
                .productId(id)
                .productName("Product " + id)
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .category(category)
                .build();
    }

    private Review createTestReview(Long reviewId, User user, Product product, Integer rating) {
        return Review.builder()
                .reviewId(reviewId)
                .rating(rating)
                .comment("Great product!")
                .createdAt(LocalDateTime.now())
                .user(user)
                .product(product)
                .build();
    }

    private ReviewResponseDTO createTestReviewResponseDTO(Review review) {
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

    @Test
    void createReview_Success() {
        User user = createTestUser(1L);
        Product product = createTestProduct(1L);

        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(1L)
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .build();

        Review review = Review.builder()
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .user(user)
                .product(product)
                .build();

        Review savedReview = createTestReview(1L, user, product, 5);
        ReviewResponseDTO expectedResponse = createTestReviewResponseDTO(savedReview);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserUserIdAndProductProductId(1L, 1L)).thenReturn(false);
        when(reviewMapper.toEntity(request, user, product)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(expectedResponse);

        ReviewResponseDTO result = reviewService.createReview(request);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals(5, result.getRating());
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getUserId());

        verify(currentUserService).getCurrentUser();
        verify(productRepository).findById(1L);
        verify(reviewRepository).existsByUserUserIdAndProductProductId(1L, 1L);
        verify(reviewMapper).toEntity(request, user, product);
        verify(reviewRepository).save(review);
        verify(reviewMapper).toResponse(savedReview);
    }

    @Test
    void createReview_ProductNotFound() {
        User user = createTestUser(1L);

        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(999L)
                .rating(4)
                .comment("Test comment")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> reviewService.createReview(request)
        );

        assertTrue(exception.getMessage().contains("999"));

        verify(currentUserService).getCurrentUser();
        verify(productRepository).findById(999L);
        verify(reviewRepository, never()).existsByUserUserIdAndProductProductId(anyLong(), anyLong());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_AlreadyExists() {
        User user = createTestUser(1L);
        Product product = createTestProduct(1L);

        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(1L)
                .rating(4)
                .comment("Duplicate review attempt")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserUserIdAndProductProductId(1L, 1L)).thenReturn(true);

        ReviewAlreadyExistsException exception = assertThrows(
                ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(request)
        );

        assertTrue(exception.getMessage().contains("already reviewed"));

        verify(reviewRepository).existsByUserUserIdAndProductProductId(1L, 1L);
        verify(reviewMapper, never()).toEntity(any(), any(), any());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewById_Success() {
        User user = createTestUser(1L);
        Product product = createTestProduct(1L);
        Review review = createTestReview(1L, user, product, 5);
        ReviewResponseDTO expectedResponse = createTestReviewResponseDTO(review);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(expectedResponse);

        ReviewResponseDTO result = reviewService.getReviewById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals(5, result.getRating());

        verify(reviewRepository).findById(1L);
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void getReviewById_NotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.getReviewById(999L)
        );

        assertTrue(exception.getMessage().contains("999"));

        verify(reviewRepository).findById(999L);
        verify(reviewMapper, never()).toResponse(any(Review.class));
    }

    @Test
    void getReviewsByProductId_Success() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        Product product = createTestProduct(1L);

        Review review1 = createTestReview(1L, user1, product, 5);
        Review review2 = createTestReview(2L, user2, product, 4);

        List<Review> reviews = List.of(review1, review2);

        when(reviewRepository.findByProductProductId(1L)).thenReturn(reviews);
        when(reviewMapper.toResponse(review1)).thenReturn(createTestReviewResponseDTO(review1));
        when(reviewMapper.toResponse(review2)).thenReturn(createTestReviewResponseDTO(review2));

        List<ReviewResponseDTO> result = reviewService.getReviewsByProductId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getRating());
        assertEquals(4, result.get(1).getRating());

        verify(reviewRepository).findByProductProductId(1L);
        verify(reviewMapper).toResponse(review1);
        verify(reviewMapper).toResponse(review2);
    }

    @Test
    void getReviewsByProductId_EmptyList() {
        when(reviewRepository.findByProductProductId(999L)).thenReturn(List.of());

        List<ReviewResponseDTO> result = reviewService.getReviewsByProductId(999L);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(reviewRepository).findByProductProductId(999L);
        verify(reviewMapper, never()).toResponse(any(Review.class));
    }

    @Test
    void getReviewsByCurrentUser_Success() {
        User user = createTestUser(1L);
        Product product1 = createTestProduct(1L);
        Product product2 = createTestProduct(2L);

        Review review1 = createTestReview(1L, user, product1, 5);
        Review review2 = createTestReview(2L, user, product2, 4);

        List<Review> reviews = List.of(review1, review2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(reviews);
        when(reviewMapper.toResponse(review1)).thenReturn(createTestReviewResponseDTO(review1));
        when(reviewMapper.toResponse(review2)).thenReturn(createTestReviewResponseDTO(review2));

        List<ReviewResponseDTO> result = reviewService.getReviewsByCurrentUser();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(currentUserService).getCurrentUser();
        verify(reviewRepository).findByUserUserId(1L);
    }

    @Test
    void updateReview_Success() {
        User user = createTestUser(1L);
        Product product = createTestProduct(1L);
        Review review = createTestReview(1L, user, product, 4);

        UpdateReviewRequestDTO request = UpdateReviewRequestDTO.builder()
                .rating(5)
                .comment("Updated comment - amazing product!")
                .build();

        Review updatedReview = createTestReview(1L, user, product, 5);
        updatedReview.setComment("Updated comment - amazing product!");

        ReviewResponseDTO expectedResponse = createTestReviewResponseDTO(updatedReview);
        expectedResponse.setComment("Updated comment - amazing product!");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(updatedReview);
        when(reviewMapper.toResponse(updatedReview)).thenReturn(expectedResponse);

        ReviewResponseDTO result = reviewService.updateReview(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals(5, result.getRating());
        assertEquals("Updated comment - amazing product!", result.getComment());

        verify(currentUserService).getCurrentUser();
        verify(reviewRepository).findById(1L);
        verify(reviewMapper).updateReviewFromRequest(request, review);
        verify(reviewRepository).save(review);
        verify(reviewMapper).toResponse(updatedReview);
    }

    @Test
    void updateReview_NotFound() {
        User user = createTestUser(1L);
        UpdateReviewRequestDTO request = UpdateReviewRequestDTO.builder()
                .rating(5).comment("test").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(999L, request)
        );

        assertTrue(exception.getMessage().contains("999"));

        verify(reviewRepository).findById(999L);
        verify(reviewMapper, never()).updateReviewFromRequest(any(), any());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_NotOwner() {
        User owner = createTestUser(1L);
        User anotherUser = createTestUser(2L);
        Product product = createTestProduct(1L);
        Review review = createTestReview(1L, owner, product, 5);

        UpdateReviewRequestDTO request = UpdateReviewRequestDTO.builder()
                .rating(3).comment("hacked attempt").build();

        when(currentUserService.getCurrentUser()).thenReturn(anotherUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(1L, request)
        );

        assertTrue(exception.getMessage().contains("not authorized"));

        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_Success() {
        User user = createTestUser(1L);
        Product product = createTestProduct(1L);
        Review review = createTestReview(1L, user, product, 5);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        DeleteReviewResponseDTO result = reviewService.deleteReview(1L);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals("DELETED", result.getState());

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_NotFound() {
        User user = createTestUser(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(999L)
        );

        assertTrue(exception.getMessage().contains("999"));

        verify(reviewRepository).findById(999L);
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void deleteReview_NotOwner() {
        User owner = createTestUser(1L);
        User anotherUser = createTestUser(2L);
        Product product = createTestProduct(1L);
        Review review = createTestReview(1L, owner, product, 5);

        when(currentUserService.getCurrentUser()).thenReturn(anotherUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(1L)
        );

        assertTrue(exception.getMessage().contains("not authorized"));

        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).delete(any(Review.class));
    }
}
