package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.repository.CategoryRepository;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final JsonMapper jsonMapper =
            JsonMapper.builder().build();

    private String userToken;

    private Long testCategoryId;

    private String testEmail;


    @BeforeEach
    void setup() throws Exception {

        /*
         * Clean dependent data first.
         *
         * Product -> Category
         *
         * Therefore products must be deleted before categories.
         */
        productRepository.deleteAll();

        categoryRepository.deleteAll();

        /*
         * Remove users created by previous test executions.
         *
         * This is important because email is normally UNIQUE.
         */
        userRepository.deleteAll();


        /*
         * Generate a unique email for every test execution.
         *
         * This prevents:
         *
         * 409 Conflict
         * Email already registered
         */
        testEmail =
                "productuser_" +
                        UUID.randomUUID() +
                        "@gmail.com";


        RegisterRequestDTO register =
                RegisterRequestDTO.builder()
                        .fullName("Product User")
                        .email(testEmail)
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                register))
                )
                .andDo(print())
                .andExpect(status().isCreated());


        LoginRequestDTO login =
                LoginRequestDTO.builder()
                        .email(testEmail)
                        .password("Password@123")
                        .build();


        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(
                                                        login))
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();


        userToken =
                jsonMapper
                        .readTree(
                                loginResult
                                        .getResponse()
                                        .getContentAsString())
                        .get("data")
                        .get("token")
                        .stringValue();


        /*
         * Create common category for tests.
         */
        testCategoryId =
                createCategory(
                        "ProductsCategory",
                        "Products");
    }


    private Long createCategory(
            String name,
            String description) throws Exception {

        CreateCategoryRequestDTO request =
                CreateCategoryRequestDTO.builder()
                        .categoryName(name)
                        .description(description)
                        .build();


        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/categories")
                                        .header(
                                                "Authorization",
                                                "Bearer " + userToken)
                                        .contentType(
                                                MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(
                                                        request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andReturn();


        return jsonMapper
                .readTree(
                        result
                                .getResponse()
                                .getContentAsString())
                .get("data")
                .get("categoryId")
                .asLong();
    }


    // ============================================================
    // CREATE PRODUCT
    // ============================================================


    @Test
    void createProduct_Success_AllFields() throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Gaming Laptop")
                        .description(
                                "High-end gaming laptop with RTX 4090")
                        .brand("GigaBrand")
                        .categoryId(testCategoryId)
                        .price(BigDecimal.valueOf(1899.99))
                        .stockQuantity(50)
                        .imageUrl(
                                "https://example.com/laptop.jpg")
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Product created successfully"))
                .andExpect(
                        jsonPath("$.data.productName")
                                .value("Gaming Laptop"))
                .andExpect(
                        jsonPath("$.data.description")
                                .value(
                                        "High-end gaming laptop with RTX 4090"))
                .andExpect(
                        jsonPath("$.data.brand")
                                .value("GigaBrand"))
                .andExpect(
                        jsonPath("$.data.price")
                                .value(1899.99))
                .andExpect(
                        jsonPath("$.data.stockQuantity")
                                .value(50))
                .andExpect(
                        jsonPath("$.data.categoryName")
                                .value("ProductsCategory"))
                .andExpect(
                        jsonPath("$.data.productId")
                                .exists());

        assertEquals(1, productRepository.count());
    }


    @Test
    void createProduct_CategoryNotFound_404()
            throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Ghost Product")
                        .description("Test product")
                        .brand("TestBrand")
                        .categoryId(999999L)
                        .price(BigDecimal.TEN)
                        .stockQuantity(10)
                        .imageUrl("https://example.com/ghost.jpg")
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        assertEquals(0, productRepository.count());
    }


    @Test
    void createProduct_MissingFields_BadRequest()
            throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName(null)
                        .description(null)
                        .brand(null)
                        .categoryId(null)
                        .price(null)
                        .stockQuantity(null)
                        .imageUrl(null)
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());


        assertEquals(0, productRepository.count());
    }


    @Test
    void createProduct_NegativePrice_BadRequest()
            throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Negative Price Product")
                        .description("Test")
                        .brand("TestBrand")
                        .categoryId(testCategoryId)
                        .price(BigDecimal.valueOf(-10))
                        .stockQuantity(10)
                        .imageUrl("https://example.com/test.jpg")
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void createProduct_Unauthorized_403()
            throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName("Unauthorized Product")
                        .description("Test")
                        .brand("TestBrand")
                        .categoryId(testCategoryId)
                        .price(BigDecimal.ONE)
                        .stockQuantity(10)
                        .imageUrl("https://example.com/test.jpg")
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    // ============================================================
    // GET ALL PRODUCTS
    // ============================================================


    @Test
    void getAllProducts_Public_Empty()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/products")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value(200))
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(0));
    }


    @Test
    void getAllProducts_Public_MultipleProducts()
            throws Exception {

        createProduct("Phone", 499.99);
        createProduct("Tablet", 299.99);
        createProduct("Headphones", 89.99);


        mockMvc.perform(
                        get("/api/v1/products")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(3));
    }


    // ============================================================
    // GET PRODUCT BY ID
    // ============================================================


    @Test
    void getProductById_Success()
            throws Exception {

        Long productId =
                createProduct(
                        "Smart Watch",
                        199.99);


        mockMvc.perform(
                        get(
                                "/api/v1/products/"
                                        + productId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.productName")
                                .value("Smart Watch"))
                .andExpect(
                        jsonPath("$.data.price")
                                .value(199.99));
    }


    @Test
    void getProductById_NotFound()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/products/999999")
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    // ============================================================
    // UPDATE PRODUCT
    // ============================================================


    @Test
    void updateProduct_AllFieldsUpdated_Success()
            throws Exception {

        Long productId =
                createProduct(
                        "Old Phone",
                        100.0);


        Long otherCategoryId =
                createCategory(
                        "OtherCategory",
                        "Other category");


        UpdateProductRequestDTO request =
                UpdateProductRequestDTO.builder()
                        .productName("New Phone Model")
                        .description("Upgraded description")
                        .brand("NewBrand")
                        .categoryId(otherCategoryId)
                        .price(BigDecimal.valueOf(250.0))
                        .stockQuantity(200)
                        .imageUrl(
                                "https://example.com/new-phone.jpg")
                        .build();


        mockMvc.perform(
                        put(
                                "/api/v1/products/"
                                        + productId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.productName")
                                .value("New Phone Model"))
                .andExpect(
                        jsonPath("$.data.description")
                                .value("Upgraded description"))
                .andExpect(
                        jsonPath("$.data.brand")
                                .value("NewBrand"))
                .andExpect(
                        jsonPath("$.data.price")
                                .value(250.0))
                .andExpect(
                        jsonPath("$.data.stockQuantity")
                                .value(200))
                .andExpect(
                        jsonPath("$.data.categoryName")
                                .value("OtherCategory"));
    }


    @Test
    void updateProduct_NotFound()
            throws Exception {

        UpdateProductRequestDTO request =
                UpdateProductRequestDTO.builder()
                        .productName("Ghost")
                        .description("Ghost")
                        .brand("GhostBrand")
                        .categoryId(testCategoryId)
                        .price(BigDecimal.ONE)
                        .stockQuantity(10)
                        .imageUrl("https://example.com/ghost.jpg")
                        .build();


        mockMvc.perform(
                        put("/api/v1/products/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void updateProduct_InvalidCategory_NotFound()
            throws Exception {

        Long productId =
                createProduct(
                        "Update Test Product",
                        10.0);


        UpdateProductRequestDTO request =
                UpdateProductRequestDTO.builder()
                        .productName("Updated")
                        .description("Updated")
                        .brand("UpdatedBrand")
                        .categoryId(999999L)
                        .price(BigDecimal.ONE)
                        .stockQuantity(10)
                        .imageUrl("https://example.com/update.jpg")
                        .build();


        mockMvc.perform(
                        put(
                                "/api/v1/products/"
                                        + productId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    // ============================================================
    // DELETE PRODUCT
    // ============================================================


    @Test
    void deleteProduct_Success_StateDeleted()
            throws Exception {

        Long productId =
                createProduct(
                        "Delete Me",
                        10.0);


        mockMvc.perform(
                        delete(
                                "/api/v1/products/"
                                        + productId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.state")
                                .value("DELETED"))
                .andExpect(
                        jsonPath("$.data.productId")
                                .value(productId.intValue()))
                .andExpect(
                        jsonPath("$.data.productName")
                                .value("Delete Me"));


        assertEquals(
                0,
                productRepository.count());
    }


    @Test
    void deleteProduct_NotFound()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/v1/products/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteProduct_Unauthorized_403()
            throws Exception {

        Long productId =
                createProduct(
                        "Protected Product",
                        10.0);


        mockMvc.perform(
                        delete(
                                "/api/v1/products/"
                                        + productId)
                )
                .andDo(print())
                .andExpect(status().isForbidden());


        assertEquals(
                1,
                productRepository.count());
    }


    // ============================================================
    // HELPER
    // ============================================================


    private Long createProduct(
            String name,
            double price)
            throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName(name)
                        .description(
                                name + " description")
                        .brand("BrandX")
                        .categoryId(testCategoryId)
                        .price(
                                BigDecimal.valueOf(price))
                        .stockQuantity(100)
                        .imageUrl(
                                "https://example.com/"
                                        + name
                                        + ".jpg")
                        .build();


        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/products")
                                        .header(
                                                "Authorization",
                                                "Bearer " + userToken)
                                        .contentType(
                                                MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(
                                                        request))
                        )
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andReturn();


        return jsonMapper
                .readTree(
                        result
                                .getResponse()
                                .getContentAsString())
                .get("data")
                .get("productId")
                .asLong();
    }
}