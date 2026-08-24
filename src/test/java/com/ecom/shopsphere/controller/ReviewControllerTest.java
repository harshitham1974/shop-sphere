package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteReviewResponseDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.ReviewAlreadyExistsException;
import com.ecom.shopsphere.exception.ReviewNotFoundException;
import com.ecom.shopsphere.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import com.ecom.shopsphere.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createReview_Success() throws Exception {

        CreateReviewRequestDTO request =
                CreateReviewRequestDTO.builder()
                        .productId(1L)
                        .rating(5)
                        .comment("Excellent product! Highly recommended.")
                        .build();

        ReviewResponseDTO response =
                ReviewResponseDTO.builder()
                        .reviewId(1L)
                        .rating(5)
                        .comment("Excellent product! Highly recommended.")
                        .createdAt(LocalDateTime.now())
                        .userId(1L)
                        .userName("John Doe")
                        .productId(1L)
                        .productName("Smart Phone")
                        .build();

        when(reviewService.createReview(any(CreateReviewRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Review created successfully"))
                .andExpect(jsonPath("$.data.reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data.rating")
                        .value(5))
                .andExpect(jsonPath("$.data.comment")
                        .value("Excellent product! Highly recommended."))
                .andExpect(jsonPath("$.data.userId")
                        .value(1))
                .andExpect(jsonPath("$.data.userName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data.productId")
                        .value(1))
                .andExpect(jsonPath("$.data.productName")
                        .value("Smart Phone"));

        verify(reviewService)
                .createReview(any(CreateReviewRequestDTO.class));
    }

    @Test
    void getReviewById_Success() throws Exception {

        Long reviewId = 1L;

        ReviewResponseDTO response =
                ReviewResponseDTO.builder()
                        .reviewId(reviewId)
                        .rating(4)
                        .comment("Great product, good value for money.")
                        .createdAt(LocalDateTime.now())
                        .userId(2L)
                        .userName("Jane Smith")
                        .productId(1L)
                        .productName("Smart Phone")
                        .build();

        when(reviewService.getReviewById(eq(reviewId)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/reviews/{reviewId}", reviewId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Review fetched successfully"))
                .andExpect(jsonPath("$.data.reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data.rating")
                        .value(4))
                .andExpect(jsonPath("$.data.comment")
                        .value("Great product, good value for money."))
                .andExpect(jsonPath("$.data.userName")
                        .value("Jane Smith"));

        verify(reviewService)
                .getReviewById(eq(reviewId));
    }

    @Test
    void getReviewsByProductId_Success() throws Exception {

        Long productId = 1L;

        ReviewResponseDTO review1 =
                ReviewResponseDTO.builder()
                        .reviewId(1L)
                        .rating(5)
                        .comment("Excellent product!")
                        .createdAt(LocalDateTime.now())
                        .userId(1L)
                        .userName("John Doe")
                        .productId(productId)
                        .productName("Smart Phone")
                        .build();

        ReviewResponseDTO review2 =
                ReviewResponseDTO.builder()
                        .reviewId(2L)
                        .rating(4)
                        .comment("Good quality, fast shipping.")
                        .createdAt(LocalDateTime.now())
                        .userId(2L)
                        .userName("Jane Smith")
                        .productId(productId)
                        .productName("Smart Phone")
                        .build();

        List<ReviewResponseDTO> response = List.of(review1, review2);

        when(reviewService.getReviewsByProductId(eq(productId)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/reviews/product/{productId}", productId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Reviews fetched successfully"))
                .andExpect(jsonPath("$.data[0].reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].rating")
                        .value(5))
                .andExpect(jsonPath("$.data[0].comment")
                        .value("Excellent product!"))
                .andExpect(jsonPath("$.data[1].reviewId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].rating")
                        .value(4))
                .andExpect(jsonPath("$.data[1].comment")
                        .value("Good quality, fast shipping."));

        verify(reviewService)
                .getReviewsByProductId(eq(productId));
    }

    @Test
    void getMyReviews_Success() throws Exception {

        ReviewResponseDTO review1 =
                ReviewResponseDTO.builder()
                        .reviewId(1L)
                        .rating(5)
                        .comment("Excellent product!")
                        .createdAt(LocalDateTime.now())
                        .userId(1L)
                        .userName("John Doe")
                        .productId(1L)
                        .productName("Smart Phone")
                        .build();

        ReviewResponseDTO review2 =
                ReviewResponseDTO.builder()
                        .reviewId(2L)
                        .rating(4)
                        .comment("Good headphones.")
                        .createdAt(LocalDateTime.now())
                        .userId(1L)
                        .userName("John Doe")
                        .productId(2L)
                        .productName("Wireless Headphones")
                        .build();

        List<ReviewResponseDTO> response = List.of(review1, review2);

        when(reviewService.getReviewsByCurrentUser())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/reviews/my-reviews")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Reviews fetched successfully"))
                .andExpect(jsonPath("$.data[0].reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].productName")
                        .value("Smart Phone"))
                .andExpect(jsonPath("$.data[1].reviewId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].productName")
                        .value("Wireless Headphones"));

        verify(reviewService)
                .getReviewsByCurrentUser();
    }

    @Test
    void updateReview_Success() throws Exception {

        Long reviewId = 1L;

        UpdateReviewRequestDTO request =
                UpdateReviewRequestDTO.builder()
                        .rating(5)
                        .comment("Updated comment: Even better than expected!")
                        .build();

        ReviewResponseDTO response =
                ReviewResponseDTO.builder()
                        .reviewId(reviewId)
                        .rating(5)
                        .comment("Updated comment: Even better than expected!")
                        .createdAt(LocalDateTime.now())
                        .userId(1L)
                        .userName("John Doe")
                        .productId(1L)
                        .productName("Smart Phone")
                        .build();

        when(reviewService.updateReview(eq(reviewId), any(UpdateReviewRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/reviews/{reviewId}", reviewId)
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Review updated successfully"))
                .andExpect(jsonPath("$.data.reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data.rating")
                        .value(5))
                .andExpect(jsonPath("$.data.comment")
                        .value("Updated comment: Even better than expected!"));

        verify(reviewService)
                .updateReview(eq(reviewId), any(UpdateReviewRequestDTO.class));
    }

    @Test
    void deleteReview_Success() throws Exception {

        Long reviewId = 1L;

        DeleteReviewResponseDTO response =
                DeleteReviewResponseDTO.builder()
                        .reviewId(reviewId)
                        .state("DELETED")
                        .build();

        when(reviewService.deleteReview(eq(reviewId)))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/reviews/{reviewId}", reviewId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Review deleted successfully"))
                .andExpect(jsonPath("$.data.reviewId")
                        .value(1))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(reviewService)
                .deleteReview(eq(reviewId));
    }

    @Test
    void getReviewById_ReviewNotFoundException() throws Exception {

        Long reviewId = 999L;

        when(reviewService.getReviewById(eq(reviewId)))
                .thenThrow(
                        new ReviewNotFoundException(
                                "Review Not Found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/reviews/{reviewId}", reviewId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Review Not Found"));

        verify(reviewService)
                .getReviewById(eq(reviewId));
    }

    @Test
    void createReview_ProductNotFoundException() throws Exception {

        CreateReviewRequestDTO request =
                CreateReviewRequestDTO.builder()
                        .productId(999L)
                        .rating(5)
                        .comment("Great product!")
                        .build();

        when(reviewService.createReview(any(CreateReviewRequestDTO.class)))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product Not Found"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product Not Found"));

        verify(reviewService)
                .createReview(any(CreateReviewRequestDTO.class));
    }

    @Test
    void createReview_ReviewAlreadyExistsException() throws Exception {

        CreateReviewRequestDTO request =
                CreateReviewRequestDTO.builder()
                        .productId(1L)
                        .rating(5)
                        .comment("Great product!")
                        .build();

        when(reviewService.createReview(any(CreateReviewRequestDTO.class)))
                .thenThrow(
                        new ReviewAlreadyExistsException(
                                "Review Already Exists"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Review Already Exists"));

        verify(reviewService)
                .createReview(any(CreateReviewRequestDTO.class));
    }
}
