package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.repository.CategoryRepository;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.ReviewRepository;
import com.ecom.shopsphere.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private String reviewerToken;
    private String anotherUserToken;
    private Long testProductId;

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        RegisterRequestDTO reviewer = RegisterRequestDTO.builder()
                .fullName("Review User")
                .email("reviewer@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(reviewer))
        ).andExpect(status().isCreated());

        reviewerToken = extractTokenFromLogin("reviewer@gmail.com", "Password@123");

        RegisterRequestDTO another = RegisterRequestDTO.builder()
                .fullName("Another User")
                .email("another@gmail.com")
                .password("Password@456")
                .phoneNumber("9876543211")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(another))
        ).andExpect(status().isCreated());

        anotherUserToken = extractTokenFromLogin("another@gmail.com", "Password@456");

        CreateCategoryRequestDTO createCategory = CreateCategoryRequestDTO.builder()
                .categoryName("Books")
                .description("All kinds of books")
                .build();

        MvcResult categoryResult = mockMvc.perform(
                post("/api/v1/categories")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createCategory))
        )
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = jsonMapper
                .readTree(categoryResult.getResponse().getContentAsString())
                .get("data")
                .get("categoryId")
                .asLong();

        CreateProductRequestDTO createProduct = CreateProductRequestDTO.builder()
                .productName("Test Book")
                .description("An amazing test book")
                .brand("Test Publishers")
                .categoryId(categoryId)
                .price(BigDecimal.valueOf(49.99))
                .stockQuantity(500)
                .imageUrl("https://example.com/book.jpg")
                .build();

        MvcResult productResult = mockMvc.perform(
                post("/api/v1/products")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createProduct))
        )
                .andExpect(status().isCreated())
                .andReturn();

        testProductId = jsonMapper
                .readTree(productResult.getResponse().getContentAsString())
                .get("data")
                .get("productId")
                .asLong();
    }

    private String extractTokenFromLogin(String email, String password) throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        return jsonMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .stringValue();
    }

    @Test
    void createReview_Success() throws Exception {
        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("Absolutely love this product!")
                .build();

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Absolutely love this product!"))
                .andExpect(jsonPath("$.data.productId").value(testProductId.intValue()));

        assertTrue(reviewRepository.count() > 0);
    }

    @Test
    void createReview_Duplicate_Conflict() throws Exception {
        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("First review")
                .build();

        mockMvc.perform(
                post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());

        CreateReviewRequestDTO duplicate = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Duplicate attempt")
                .build();

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(duplicate))
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Review Already Exists"));
    }

    @Test
    void createReview_ProductNotFound() throws Exception {
        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(9999L)
                .rating(5)
                .comment("No product")
                .build();

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product Not Found"));
    }

    @Test
    void createReview_InvalidRating_BadRequest() throws Exception {
        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(10)
                .comment("Invalid rating")
                .build();

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_Unauthorized() throws Exception {
        CreateReviewRequestDTO request = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("No auth")
                .build();

        mockMvc.perform(
                        post("/api/v1/reviews")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getReviewById_Success() throws Exception {
        CreateReviewRequestDTO createRequest = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Great product")
                .build();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = jsonMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("reviewId")
                .asLong();

        mockMvc.perform(
                        get("/api/v1/reviews/" + reviewId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(reviewId.intValue()))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    void getReviewById_NotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/reviews/99999")
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review Not Found"));
    }

    @Test
    void getReviewsByProductId_Success() throws Exception {
        CreateReviewRequestDTO first = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("First review comment")
                .build();

        CreateReviewRequestDTO second = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Second review comment")
                .build();

        mockMvc.perform(
                post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(first))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(second))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/reviews/product/" + testProductId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getReviewsByProductId_Empty() throws Exception {
        mockMvc.perform(
                        get("/api/v1/reviews/product/" + testProductId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getMyReviews_Success() throws Exception {
        CreateReviewRequestDTO first = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("Mine")
                .build();

        CreateReviewRequestDTO other = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Someone else's")
                .build();

        mockMvc.perform(
                post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(first))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(other))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/reviews/my-reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].comment").value("Mine"));
    }

    @Test
    void getMyReviews_Unauthorized() throws Exception {
        mockMvc.perform(
                        get("/api/v1/reviews/my-reviews")
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReview_Success() throws Exception {
        CreateReviewRequestDTO createRequest = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Original comment")
                .build();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = jsonMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("reviewId")
                .asLong();

        UpdateReviewRequestDTO updateRequest = UpdateReviewRequestDTO.builder()
                .rating(5)
                .comment("Updated comment - even better!")
                .build();

        mockMvc.perform(
                        put("/api/v1/reviews/" + reviewId)
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(updateRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Updated comment - even better!"));
    }

    @Test
    void updateReview_NotOwner_NotFound() throws Exception {
        CreateReviewRequestDTO createRequest = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(4)
                .comment("Not mine")
                .build();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = jsonMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("reviewId")
                .asLong();

        UpdateReviewRequestDTO updateRequest = UpdateReviewRequestDTO.builder()
                .rating(1).comment("Hacked!").build();

        mockMvc.perform(
                        put("/api/v1/reviews/" + reviewId)
                                .header("Authorization", "Bearer " + anotherUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(updateRequest))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review Not Found"));
    }

    @Test
    void updateReview_NotFound() throws Exception {
        UpdateReviewRequestDTO updateRequest = UpdateReviewRequestDTO.builder()
                .rating(5).comment("No review").build();

        mockMvc.perform(
                        put("/api/v1/reviews/99999")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(updateRequest))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_Success() throws Exception {
        CreateReviewRequestDTO createRequest = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(3)
                .comment("To delete")
                .build();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = jsonMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("reviewId")
                .asLong();

        mockMvc.perform(
                        delete("/api/v1/reviews/" + reviewId)
                                .header("Authorization", "Bearer " + reviewerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("DELETED"));

        assertFalse(reviewRepository.existsById(reviewId));
    }

    @Test
    void deleteReview_NotOwner_NotFound() throws Exception {
        CreateReviewRequestDTO createRequest = CreateReviewRequestDTO.builder()
                .productId(testProductId)
                .rating(5)
                .comment("Owner only can delete")
                .build();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/reviews")
                                .header("Authorization", "Bearer " + reviewerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = jsonMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("reviewId")
                .asLong();

        mockMvc.perform(
                        delete("/api/v1/reviews/" + reviewId)
                                .header("Authorization", "Bearer " + anotherUserToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        assertTrue(reviewRepository.existsById(reviewId));
    }

    @Test
    void deleteReview_NotFound() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/reviews/99999")
                                .header("Authorization", "Bearer " + reviewerToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
