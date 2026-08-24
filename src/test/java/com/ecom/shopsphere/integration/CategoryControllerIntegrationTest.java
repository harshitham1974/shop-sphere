package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // ADDED:
    // We need this because setup() creates the same user before every test.
    @Autowired
    private UserRepository userRepository;

    private String userToken;


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setup() throws Exception {

        /*
         * IMPORTANT:
         * Delete products first because Product has a relationship
         * with Category.
         */
        productRepository.deleteAll();

        /*
         * Delete categories after products.
         */
        categoryRepository.deleteAll();

        /*
         * ADDED:
         * Delete users because every test creates
         * categoryuser@gmail.com again.
         *
         * Without this line, running the FULL CLASS can produce:
         *
         * Expected: 201
         * Actual:   409
         *
         * because the user already exists.
         */
        userRepository.deleteAll();


        // ---------------------------------------------------------
        // Create test user
        // ---------------------------------------------------------

        RegisterRequestDTO register =
                RegisterRequestDTO.builder()
                        .fullName("Category User")
                        .email("categoryuser@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(register)
                                )
                )
                .andExpect(status().isCreated());


        // ---------------------------------------------------------
        // Login test user
        // ---------------------------------------------------------

        LoginRequestDTO login =
                LoginRequestDTO.builder()
                        .email("categoryuser@gmail.com")
                        .password("Password@123")
                        .build();


        MvcResult loginRes =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(login)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        /*
         * Extract JWT token from:
         *
         * {
         *     "data": {
         *         "token": "..."
         *     }
         * }
         *
         * stringValue() is deprecated in newer Jackson.
         * asText() is the replacement.
         */
        userToken =
                jsonMapper
                        .readTree(
                                loginRes.getResponse()
                                        .getContentAsString()
                        )
                        .get("data")
                        .get("token")
                        .asText();
    }


    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    @Test
    void createCategory_Success() throws Exception {

        CreateCategoryRequestDTO req =
                CreateCategoryRequestDTO.builder()
                        .categoryName("Furniture")
                        .description("Home and office furniture")
                        .build();


        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(req)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(
                        jsonPath("$.message")
                                .value("Category created successfully.")
                )
                .andExpect(
                        jsonPath("$.data.categoryName")
                                .value("Furniture")
                )
                .andExpect(
                        jsonPath("$.data.description")
                                .value("Home and office furniture")
                )
                .andExpect(
                        jsonPath("$.data.categoryId")
                                .exists()
                );


        assertEquals(1, categoryRepository.count());
    }


    // =========================================================
    // CREATE CATEGORY - VALIDATION
    // =========================================================

    @Test
    void createCategory_MissingName_BadRequest()
            throws Exception {

        CreateCategoryRequestDTO req =
                CreateCategoryRequestDTO.builder()
                        .categoryName(null)
                        .description("No name")
                        .build();


        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(req)
                                )
                )
                .andExpect(status().isBadRequest());


        assertEquals(0, categoryRepository.count());
    }


    // =========================================================
    // CREATE CATEGORY - DUPLICATE
    // =========================================================

    @Test
    void createCategory_DuplicateName_Conflict()
            throws Exception {

        CreateCategoryRequestDTO req =
                CreateCategoryRequestDTO.builder()
                        .categoryName("Duplicate")
                        .description("Desc 1")
                        .build();


        // First category
        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(req)
                                )
                )
                .andExpect(status().isCreated());


        // Same category name
        CreateCategoryRequestDTO duplicate =
                CreateCategoryRequestDTO.builder()
                        .categoryName("Duplicate")
                        .description("Desc 2")
                        .build();


        mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(duplicate)
                                )
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.message")
                                .value("Category Creation Failed")
                );


        // Only first category should exist
        assertEquals(1, categoryRepository.count());
    }


    // =========================================================
    // CREATE CATEGORY - UNAUTHORIZED
    // =========================================================

    @Test
    void createCategory_Unauthorized()
            throws Exception {

        CreateCategoryRequestDTO req =
                CreateCategoryRequestDTO.builder()
                        .categoryName("No Auth")
                        .description("Nope")
                        .build();


        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(req)
                                )
                )
                .andExpect(status().isForbidden());


        assertEquals(0, categoryRepository.count());
    }


    // =========================================================
    // GET ALL CATEGORIES - EMPTY
    // =========================================================

    @Test
    void getAllCategories_PublicAccess_EmptyList()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/categories")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(
                        jsonPath("$.message")
                                .value("Categories fetched successfully.")
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(0)
                );
    }


    // =========================================================
    // GET ALL CATEGORIES - MULTIPLE
    // =========================================================

    @Test
    void getAllCategories_MultipleResults()
            throws Exception {

        createCategory(
                "Books",
                "Books & stationery"
        );

        createCategory(
                "Clothing",
                "Apparel"
        );

        createCategory(
                "Gadgets",
                "Electronic gadgets"
        );


        mockMvc.perform(
                        get("/api/v1/categories")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(3)
                );
    }


    // =========================================================
    // GET CATEGORY BY ID - FOUND
    // =========================================================

    @Test
    void getCategoryById_Found()
            throws Exception {

        Long id =
                createCategory(
                        "Kitchen",
                        "Kitchenware"
                );


        mockMvc.perform(
                        get("/api/v1/categories/" + id)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.categoryName")
                                .value("Kitchen")
                )
                .andExpect(
                        jsonPath("$.data.description")
                                .value("Kitchenware")
                );
    }


    // =========================================================
    // GET CATEGORY BY ID - NOT FOUND
    // =========================================================

    @Test
    void getCategoryById_NotFound()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/categories/999999")
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Category Not Found")
                );
    }


    // =========================================================
    // UPDATE CATEGORY - SUCCESS
    // =========================================================

    @Test
    void updateCategory_Success_NameAndDescription()
            throws Exception {

        /*
         * First create an existing category.
         */
        Long id =
                createCategory(
                        "Old Name",
                        "Old desc"
                );


        /*
         * Now create update request.
         *
         * IMPORTANT:
         * There is NO categoryId here.
         *
         * The category ID comes from the URL:
         *
         * PUT /api/v1/categories/{id}
         */
        UpdateCategoryRequestDTO upd =
                UpdateCategoryRequestDTO.builder()
                        .categoryName("New Name")
                        .description("New updated description")
                        .build();


        mockMvc.perform(
                        put("/api/v1/categories/" + id)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(upd)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value(200)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Category updated successfully.")
                )
                .andExpect(
                        jsonPath("$.data.categoryId")
                                .value(id.intValue())
                )
                .andExpect(
                        jsonPath("$.data.categoryName")
                                .value("New Name")
                )
                .andExpect(
                        jsonPath("$.data.description")
                                .value("New updated description")
                );
    }


    // =========================================================
    // UPDATE CATEGORY - NOT FOUND
    // =========================================================

    @Test
    void updateCategory_NotFound()
            throws Exception {

        UpdateCategoryRequestDTO upd =
                UpdateCategoryRequestDTO.builder()
                        .categoryName("Ghost")
                        .description("Ghost")
                        .build();


        mockMvc.perform(
                        put("/api/v1/categories/99999")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(upd)
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Category Not Found")
                );
    }


    // =========================================================
    // DELETE CATEGORY - SUCCESS
    // =========================================================

    @Test
    void deleteCategory_Success_StateDeleted()
            throws Exception {

        Long id =
                createCategory(
                        "Delete Me",
                        "Will be deleted"
                );


        mockMvc.perform(
                        delete("/api/v1/categories/" + id)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.state")
                                .value("Category deleted successfully.")
                )
                .andExpect(
                        jsonPath("$.data.categoryId")
                                .value(id.intValue())
                );


        /*
         * The category should actually be removed
         * from the database.
         */
        assertEquals(
                0,
                categoryRepository.count()
        );
    }


    // =========================================================
    // DELETE CATEGORY - NOT FOUND
    // =========================================================

    @Test
    void deleteCategory_NotFound()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/categories/99999")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Category Not Found")
                );
    }


    // =========================================================
    // CATEGORY REFERENCED BY PRODUCT
    // =========================================================

    @Test
    void deleteCategory_StillReferencedByProducts()
            throws Exception {

        /*
         * Step 1:
         * Create category.
         */
        Long categoryId =
                createCategory(
                        "TempCat",
                        "Temp"
                );


        /*
         * Step 2:
         * Create product using that category.
         */
        CreateProductRequestDTO product =
                CreateProductRequestDTO.builder()
                        .productName("Temp Product")
                        .description("d")
                        .brand("b")
                        .categoryId(categoryId)
                        .price(BigDecimal.TEN)
                        .stockQuantity(10)
                        .imageUrl("i.jpg")
                        .build();


        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(product)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated());


        /*
         * Confirm both records exist before deletion.
         */
        assertEquals(
                1,
                categoryRepository.count()
        );

        assertEquals(
                1,
                productRepository.count()
        );


        /*
         * Step 3:
         * Now actually attempt to delete the category.
         */
        mockMvc.perform(
                        delete("/api/v1/categories/" + categoryId)
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.state")
                                .value("Category deleted successfully.")
                );


        /*
         * Whether the product remains or is deleted depends
         * on YOUR Category -> Product relationship/service logic.
         *
         * At minimum, the category should no longer exist
         * if your deleteCategory implementation is designed
         * to remove it.
         */
        assertEquals(
                0,
                categoryRepository.count()
        );
    }


    // =========================================================
    // HELPER METHOD
    // =========================================================

    private Long createCategory(
            String name,
            String desc
    ) throws Exception {

        CreateCategoryRequestDTO req =
                CreateCategoryRequestDTO.builder()
                        .categoryName(name)
                        .description(desc)
                        .build();


        MvcResult res =
                mockMvc.perform(
                                post("/api/v1/categories")
                                        .header(
                                                "Authorization",
                                                "Bearer " + userToken
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                jsonMapper.writeValueAsString(req)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn();


        return jsonMapper
                .readTree(
                        res.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("categoryId")
                .asLong();
    }
}