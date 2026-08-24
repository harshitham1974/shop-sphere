package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.*;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

class ShopSphereIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanupAll() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        cartRepository.deleteAll();
        wishlistRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndLogin(String email, String password, String fullName) throws Exception {
        RegisterRequestDTO register = RegisterRequestDTO.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(register))
        ).andExpect(status().isCreated());

        LoginRequestDTO login = LoginRequestDTO.builder()
                .email(email).password(password).build();

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(login))
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

    private Long createCategory(String token, String name) throws Exception {
        CreateCategoryRequestDTO req = CreateCategoryRequestDTO.builder()
                .categoryName(name).description(name + " desc").build();

        MvcResult res = mockMvc.perform(
                        post("/api/v1/categories")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("categoryId").asLong();
    }

    private Long createProduct(String token, Long categoryId, String name, BigDecimal price) throws Exception {
        CreateProductRequestDTO req = CreateProductRequestDTO.builder()
                .productName(name)
                .description(name + " description")
                .brand("TestBrand")
                .categoryId(categoryId)
                .price(price)
                .stockQuantity(100)
                .imageUrl("https://example.com/" + name + ".jpg")
                .build();

        MvcResult res = mockMvc.perform(
                        post("/api/v1/products")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("productId").asLong();
    }

    private Long createAddress(String token) throws Exception {
        AddAddressRequestDTO req = AddAddressRequestDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .addressLine1("123 Main Street")
                .addressLine2("Apt 4")
                .city("New Delhi")
                .state("Delhi")
                .country("India")
                .postalCode("110001")
                .defaultAddress(true)
                .build();

        MvcResult res = mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("addressId").asLong();
    }

    // ======================================================
    // AUTH CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class AuthControllerTests {

        @Test
        void register_Success() throws Exception {
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .fullName("Auth Test User")
                    .email("authtest@gmail.com")
                    .password("Password@123")
                    .phoneNumber("9876543210")
                    .build();

            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.email").value("authtest@gmail.com"));

            assertTrue(userRepository.existsByEmail("authtest@gmail.com"));
            assertEquals(1, cartRepository.count());
            assertEquals(1, wishlistRepository.count());
        }

        @Test
        void register_DuplicateEmail_Conflict() throws Exception {
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .fullName("Dup User").email("dup@gmail.com")
                    .password("Password@123").phoneNumber("9876543210").build();

            mockMvc.perform(
                    post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(req))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Registration Failed"));

            assertEquals(1, userRepository.count());
        }

        @Test
        void register_InvalidEmail_BadRequest() throws Exception {
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .fullName("No Email User")
                    .email("not-an-email")
                    .password("Password@123")
                    .phoneNumber("9876543210")
                    .build();

            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void login_Success_ReturnsToken() throws Exception {
            registerAndLogin("logintest@gmail.com", "Password@123", "Login User");
        }

        @Test
        void login_InvalidPassword_Unauthorized() throws Exception {
            RegisterRequestDTO register = RegisterRequestDTO.builder()
                    .fullName("Pwd User").email("pwd@gmail.com")
                    .password("Password@123").phoneNumber("9876543210").build();

            mockMvc.perform(
                    post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(register))
            ).andExpect(status().isCreated());

            LoginRequestDTO wrongPwd = LoginRequestDTO.builder()
                    .email("pwd@gmail.com").password("WrongPassword@1").build();

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(wrongPwd))
                    )
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Login Failed"));
        }

        @Test
        void login_UserNotFound_Unauthorized() throws Exception {
            LoginRequestDTO login = LoginRequestDTO.builder()
                    .email("nonexistent@gmail.com").password("Password@123").build();

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(login))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ======================================================
    // USER (PROFILE) CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class UserControllerTests {

        @Test
        void getProfile_Success() throws Exception {
            String token = registerAndLogin("profile@gmail.com", "Password@123", "Profile User");

            mockMvc.perform(
                            get("/api/v1/users/profile")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("profile@gmail.com"))
                    .andExpect(jsonPath("$.data.fullName").value("Profile User"));
        }

        @Test
        void getProfile_Unauthorized() throws Exception {
            mockMvc.perform(
                            get("/api/v1/users/profile")
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateProfile_Success() throws Exception {
            String token = registerAndLogin("updateprofile@gmail.com", "Password@123", "Old Name");

            UpdateProfileRequestDTO req = UpdateProfileRequestDTO.builder()
                    .fullName("Updated Name")
                    .phoneNumber("9988776655")
                    .build();

            mockMvc.perform(
                            put("/api/v1/users/update-profile")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("9988776655"));
        }

        @Test
        void changePassword_Success() throws Exception {
            String token = registerAndLogin("changepwd@gmail.com", "Password@123", "Pwd User");

            ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                    .currentPassword("Password@123")
                    .newPassword("NewPwd@456")
                    .build();

            mockMvc.perform(
                            put("/api/v1/users/change-password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.confirmation").exists());
        }

        @Test
        void changePassword_WrongOldPwd_BadRequest() throws Exception {
            String token = registerAndLogin("wrongoldpwd@gmail.com", "Password@123", "Wrong Pwd User");

            ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                    .currentPassword("WrongPassword@1")
                    .newPassword("NewPwd@456")
                    .build();

            mockMvc.perform(
                            put("/api/v1/users/change-password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void deleteAccount_Success() throws Exception {
            String token = registerAndLogin("deluser@gmail.com", "Password@123", "Delete Me");

            mockMvc.perform(
                            delete("/api/v1/users/delete-account")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("DELETED"));

            assertFalse(userRepository.existsByEmail("deluser@gmail.com"));
        }
    }

    // ======================================================
    // CATEGORY CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class CategoryControllerTests {

        @Test
        void createCategory_Success() throws Exception {
            String token = registerAndLogin("catuser@gmail.com", "Password@123", "Cat User");

            CreateCategoryRequestDTO req = CreateCategoryRequestDTO.builder()
                    .categoryName("Toys").description("Kids toys").build();

            mockMvc.perform(
                            post("/api/v1/categories")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.categoryName").value("Toys"));

            assertEquals(1, categoryRepository.count());
        }

        @Test
        void createCategory_DuplicateName_Conflict() throws Exception {
            String token = registerAndLogin("catdup@gmail.com", "Password@123", "Cat Dup User");

            CreateCategoryRequestDTO req = CreateCategoryRequestDTO.builder()
                    .categoryName("DupCat").description("desc").build();

            mockMvc.perform(
                    post("/api/v1/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(req))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            post("/api/v1/categories")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isConflict());
        }

        @Test
        void getAllCategories_Public() throws Exception {
            String token = registerAndLogin("catget@gmail.com", "Password@123", "Cat Get User");
            createCategory(token, "Books");
            createCategory(token, "Music");

            mockMvc.perform(
                            get("/api/v1/categories")
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        void getCategoryById_Success() throws Exception {
            String token = registerAndLogin("catbyid@gmail.com", "Password@123", "Cat By ID");
            Long catId = createCategory(token, "Movies");

            mockMvc.perform(
                            get("/api/v1/categories/" + catId)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.categoryName").value("Movies"));
        }

        @Test
        void getCategoryById_NotFound() throws Exception {
            mockMvc.perform(
                            get("/api/v1/categories/99999")
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateCategory_Success() throws Exception {
            String token = registerAndLogin("catupd@gmail.com", "Password@123", "Cat Upd");
            Long catId = createCategory(token, "OldName");

            UpdateCategoryRequestDTO req = UpdateCategoryRequestDTO.builder()
                    .categoryName("NewName").description("Updated desc").build();

            mockMvc.perform(
                            put("/api/v1/categories/" + catId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.categoryName").value("NewName"));
        }

        @Test
        void deleteCategory_Success() throws Exception {
            String token = registerAndLogin("catdel@gmail.com", "Password@123", "Cat Del");
            Long catId = createCategory(token, "DeleteMe");

            mockMvc.perform(
                            delete("/api/v1/categories/" + catId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("Category deleted successfully."));

            assertEquals(0, categoryRepository.count());
        }
    }

    // ======================================================
    // PRODUCT CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class ProductControllerTests {

        @Test
        void createProduct_Success() throws Exception {
            String token = registerAndLogin("puser@gmail.com", "Password@123", "P User");
            Long catId = createCategory(token, "Electronics");

            CreateProductRequestDTO req = CreateProductRequestDTO.builder()
                    .productName("Laptop")
                    .description("Powerful laptop")
                    .brand("Tech")
                    .categoryId(catId)
                    .price(BigDecimal.valueOf(999.99))
                    .stockQuantity(50)
                    .imageUrl("https://example.com/laptop.jpg")
                    .build();

            mockMvc.perform(
                            post("/api/v1/products")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.productName").value("Laptop"))
                    .andExpect(jsonPath("$.data.price").value(999.99));

            assertEquals(1, productRepository.count());
        }

        @Test
        void createProduct_CategoryNotFound() throws Exception {
            String token = registerAndLogin("pnocat@gmail.com", "Password@123", "P No Cat");

            CreateProductRequestDTO req = CreateProductRequestDTO.builder()
                    .productName("Ghost").categoryId(9999L)
                    .price(BigDecimal.TEN).stockQuantity(10)
                    .description("d").brand("b").imageUrl("i.jpg").build();

            mockMvc.perform(
                            post("/api/v1/products")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void getAllProducts_Public() throws Exception {
            String token = registerAndLogin("plist@gmail.com", "Password@123", "P List");
            Long catId = createCategory(token, "Home");
            createProduct(token, catId, "Chair", BigDecimal.valueOf(49.99));
            createProduct(token, catId, "Table", BigDecimal.valueOf(99.99));

            mockMvc.perform(
                            get("/api/v1/products")
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        void getProductById_Success() throws Exception {
            String token = registerAndLogin("pbyid@gmail.com", "Password@123", "P By ID");
            Long catId = createCategory(token, "Sports");
            Long productId = createProduct(token, catId, "Ball", BigDecimal.valueOf(9.99));

            mockMvc.perform(
                            get("/api/v1/products/" + productId)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productName").value("Ball"));
        }

        @Test
        void getProductById_NotFound() throws Exception {
            mockMvc.perform(
                            get("/api/v1/products/99999")
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateProduct_Success() throws Exception {
            String token = registerAndLogin("pupd@gmail.com", "Password@123", "P Upd");
            Long catId = createCategory(token, "Kitchen");
            Long productId = createProduct(token, catId, "Pot", BigDecimal.valueOf(25.0));
            Long newCatId = createCategory(token, "Outdoor");

            UpdateProductRequestDTO req = UpdateProductRequestDTO.builder()
                    .productName("Big Pot")
                    .description("Large cooking pot")
                    .brand("KitchenPro")
                    .categoryId(newCatId)
                    .price(BigDecimal.valueOf(35.0))
                    .stockQuantity(200)
                    .imageUrl("https://example.com/bigpot.jpg")
                    .build();

            mockMvc.perform(
                            put("/api/v1/products/" + productId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productName").value("Big Pot"))
                    .andExpect(jsonPath("$.data.price").value(35.0));
        }

        @Test
        void deleteProduct_Success() throws Exception {
            String token = registerAndLogin("pdel@gmail.com", "Password@123", "P Del");
            Long catId = createCategory(token, "Temp");
            Long productId = createProduct(token, catId, "DeleteMe", BigDecimal.ONE);

            mockMvc.perform(
                            delete("/api/v1/products/" + productId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("DELETED"));

            assertEquals(0, productRepository.count());
        }
    }

    // ======================================================
    // CART CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class CartControllerTests {

        @Test
        void addToCart_Success() throws Exception {
            String token = registerAndLogin("cartuser@gmail.com", "Password@123", "Cart User");
            Long catId = createCategory(token, "CartCat");
            Long productId = createProduct(token, catId, "Cart Item", BigDecimal.valueOf(19.99));

            AddCartItemRequestDTO req = AddCartItemRequestDTO.builder()
                    .productId(productId).quantity(3).build();

            mockMvc.perform(
                            post("/api/v1/cart/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.totalItems").value(3))
                    .andExpect(jsonPath("$.data.totalAmount").value(59.97));
        }

        @Test
        void addToCart_InvalidQuantity_BadRequest() throws Exception {
            String token = registerAndLogin("cartqty@gmail.com", "Password@123", "Cart Qty");
            Long catId = createCategory(token, "QtyCat");
            Long productId = createProduct(token, catId, "Q Item", BigDecimal.TEN);

            AddCartItemRequestDTO req = AddCartItemRequestDTO.builder()
                    .productId(productId).quantity(0).build();

            mockMvc.perform(
                            post("/api/v1/cart/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getCart_Success() throws Exception {
            String token = registerAndLogin("cartget@gmail.com", "Password@123", "Cart Get");
            Long catId = createCategory(token, "GCat");
            Long productId = createProduct(token, catId, "G Item", BigDecimal.valueOf(15.0));

            AddCartItemRequestDTO req = AddCartItemRequestDTO.builder()
                    .productId(productId).quantity(2).build();

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(req))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            get("/api/v1/cart")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items.length()").value(1));
        }

        @Test
        void updateCartItem_Success() throws Exception {
            String token = registerAndLogin("cartupd@gmail.com", "Password@123", "Cart Upd");
            Long catId = createCategory(token, "UCat");
            Long productId = createProduct(token, catId, "U Item", BigDecimal.valueOf(10.0));

            AddCartItemRequestDTO addReq = AddCartItemRequestDTO.builder()
                    .productId(productId).quantity(1).build();

            MvcResult addResult = mockMvc.perform(
                            post("/api/v1/cart/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(addReq))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long cartItemId = jsonMapper.readTree(addResult.getResponse().getContentAsString())
                    .get("data").get("items").get(0).get("cartItemId").asLong();

            UpdateCartItemRequestDTO updReq = UpdateCartItemRequestDTO.builder()
                    .quantity(5).build();

            mockMvc.perform(
                            put("/api/v1/cart/items/" + cartItemId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(updReq))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalItems").value(5));
        }

        @Test
        void removeCartItem_Success() throws Exception {
            String token = registerAndLogin("cartremove@gmail.com", "Password@123", "Cart Remove");
            Long catId = createCategory(token, "RCat");
            Long productId = createProduct(token, catId, "R Item", BigDecimal.TEN);

            AddCartItemRequestDTO addReq = AddCartItemRequestDTO.builder()
                    .productId(productId).quantity(1).build();

            MvcResult addResult = mockMvc.perform(
                            post("/api/v1/cart/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(addReq))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long cartItemId = jsonMapper.readTree(addResult.getResponse().getContentAsString())
                    .get("data").get("items").get(0).get("cartItemId").asLong();

            mockMvc.perform(
                            delete("/api/v1/cart/items/" + cartItemId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("REMOVED"));
        }

        @Test
        void clearCart_Success() throws Exception {
            String token = registerAndLogin("cartclear@gmail.com", "Password@123", "Cart Clear");
            Long catId = createCategory(token, "CCat");
            Long p1 = createProduct(token, catId, "C1", BigDecimal.ONE);
            Long p2 = createProduct(token, catId, "C2", BigDecimal.TEN);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder().productId(p1).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder().productId(p2).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            delete("/api/v1/cart")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("CLEARED"));
        }

        @Test
        void getCart_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/cart")).andExpect(status().isForbidden());
        }
    }

    // ======================================================
    // WISHLIST CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class WishlistControllerTests {

        @Test
        void addToWishlist_Success() throws Exception {
            String token = registerAndLogin("wluser@gmail.com", "Password@123", "WL User");
            Long catId = createCategory(token, "WCat");
            Long productId = createProduct(token, catId, "WL Item", BigDecimal.valueOf(50.0));

            AddWishlistItemRequestDTO req = AddWishlistItemRequestDTO.builder()
                    .productId(productId).build();

            mockMvc.perform(
                            post("/api/v1/wishlist/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.items.length()").value(1));
        }

        @Test
        void getWishlist_Success() throws Exception {
            String token = registerAndLogin("wlget@gmail.com", "Password@123", "WL Get");
            Long catId = createCategory(token, "GWCat");
            Long p1 = createProduct(token, catId, "GWI1", BigDecimal.TEN);
            Long p2 = createProduct(token, catId, "GWI2", BigDecimal.valueOf(20.0));

            mockMvc.perform(
                    post("/api/v1/wishlist/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddWishlistItemRequestDTO.builder().productId(p1).build()
                            ))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                    post("/api/v1/wishlist/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddWishlistItemRequestDTO.builder().productId(p2).build()
                            ))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            get("/api/v1/wishlist")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items.length()").value(2));
        }

        @Test
        void removeWishlistItem_Success() throws Exception {
            String token = registerAndLogin("wldel@gmail.com", "Password@123", "WL Del");
            Long catId = createCategory(token, "DWCat");
            Long productId = createProduct(token, catId, "DWI", BigDecimal.ONE);

            AddWishlistItemRequestDTO addReq = AddWishlistItemRequestDTO.builder()
                    .productId(productId).build();

            MvcResult addResult = mockMvc.perform(
                            post("/api/v1/wishlist/items")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(addReq))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long itemId = jsonMapper.readTree(addResult.getResponse().getContentAsString())
                    .get("data").get("items").get(0).get("wishlistItemId").asLong();

            mockMvc.perform(
                            delete("/api/v1/wishlist/items/" + itemId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("REMOVED"));
        }
    }

    // ======================================================
    // ADDRESS CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class AddressControllerTests {

        @Test
        void addAddress_Success() throws Exception {
            String token = registerAndLogin("adduser@gmail.com", "Password@123", "Addr User");
            Long addrId = createAddress(token);
            assertNotNull(addrId);
            assertEquals(1, addressRepository.count());
        }

        @Test
        void getAllAddresses_Success() throws Exception {
            String token = registerAndLogin("addrlist@gmail.com", "Password@123", "Addr List");
            createAddress(token);

            AddAddressRequestDTO req2 = AddAddressRequestDTO.builder()
                    .fullName("Jane").phoneNumber("9988776655")
                    .addressLine1("456 Second St").city("Mumbai")
                    .state("Maharashtra").country("India")
                    .postalCode("400001").defaultAddress(false)
                    .build();

            mockMvc.perform(
                    post("/api/v1/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(req2))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            get("/api/v1/addresses")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        void getAddressById_Success() throws Exception {
            String token = registerAndLogin("addrbyid@gmail.com", "Password@123", "Addr By ID");
            Long addrId = createAddress(token);

            mockMvc.perform(
                            get("/api/v1/addresses/" + addrId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fullName").value("John Doe"));
        }

        @Test
        void updateAddress_Success() throws Exception {
            String token = registerAndLogin("addrupd@gmail.com", "Password@123", "Addr Upd");
            Long addrId = createAddress(token);

            UpdateAddressRequestDTO req = UpdateAddressRequestDTO.builder()
                    .fullName("John Updated")
                    .phoneNumber("9988776655")
                    .addressLine1("789 Updated Rd")
                    .city("Bangalore")
                    .state("Karnataka")
                    .country("India")
                    .postalCode("560001")
                    .defaultAddress(true)
                    .build();

            mockMvc.perform(
                            put("/api/v1/addresses/" + addrId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fullName").value("John Updated"))
                    .andExpect(jsonPath("$.data.city").value("Bangalore"));
        }

        @Test
        void setDefaultAddress_Success() throws Exception {
            String token = registerAndLogin("addrdef@gmail.com", "Password@123", "Addr Def");
            Long defaultAddr = createAddress(token);

            AddAddressRequestDTO req2 = AddAddressRequestDTO.builder()
                    .fullName("Alt").phoneNumber("9988776655")
                    .addressLine1("Alt Street").city("City").state("ST")
                    .country("India").postalCode("123456")
                    .defaultAddress(false).build();

            MvcResult r2 = mockMvc.perform(
                            post("/api/v1/addresses")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req2))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long altAddrId = jsonMapper.readTree(r2.getResponse().getContentAsString())
                    .get("data").get("addressId").asLong();

            mockMvc.perform(
                            patch("/api/v1/addresses/" + altAddrId + "/default")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.defaultAddress").value(true));
        }

        @Test
        void deleteAddress_Success() throws Exception {
            String token = registerAndLogin("addrdelete@gmail.com", "Password@123", "Addr Del");
            Long addrId = createAddress(token);

            mockMvc.perform(
                            delete("/api/v1/addresses/" + addrId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("DELETED"));

            assertEquals(0, addressRepository.count());
        }
    }

    // ======================================================
    // ORDER + PAYMENT CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class OrderPaymentControllerTests {

        @Test
        void createOrder_FromCart_Success() throws Exception {
            String token = registerAndLogin("orderuser@gmail.com", "Password@123", "Order User");
            Long catId = createCategory(token, "OrderCat");
            Long productId = createProduct(token, catId, "Order Item", BigDecimal.valueOf(100.0));
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(2).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            mockMvc.perform(
                            post("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(req))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                    .andExpect(jsonPath("$.data.totalAmount").value(200.0))
                    .andExpect(jsonPath("$.data.orderItems.length()").value(1));

            assertEquals(1, orderRepository.count());
        }

        @Test
        void getMyOrders_Success() throws Exception {
            String token = registerAndLogin("myorders@gmail.com", "Password@123", "My Orders");
            Long catId = createCategory(token, "MOCat");
            Long productId = createProduct(token, catId, "MO Item", BigDecimal.valueOf(50.0));
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            mockMvc.perform(
                    post("/api/v1/orders")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(req))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            get("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        void getOrderById_Success() throws Exception {
            String token = registerAndLogin("orderbyid@gmail.com", "Password@123", "Order By ID");
            Long catId = createCategory(token, "OBICat");
            Long productId = createProduct(token, catId, "OBI Item", BigDecimal.ONE);
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO createOrd = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            MvcResult ordRes = mockMvc.perform(
                            post("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(createOrd))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                    .get("data").get("orderId").asLong();

            mockMvc.perform(
                            get("/api/v1/orders/" + orderId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value(orderId.intValue()));
        }

        @Test
        void cancelOrder_BeforeShipped_Success() throws Exception {
            String token = registerAndLogin("cancelord@gmail.com", "Password@123", "Cancel Order");
            Long catId = createCategory(token, "COCat");
            Long productId = createProduct(token, catId, "CO Item", BigDecimal.ONE);
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO createOrd = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            MvcResult ordRes = mockMvc.perform(
                            post("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(createOrd))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                    .get("data").get("orderId").asLong();

            mockMvc.perform(
                            patch("/api/v1/orders/" + orderId + "/cancel")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("CANCELLED"));
        }

        @Test
        void createPayment_ForOrder_Success() throws Exception {
            String token = registerAndLogin("payuser@gmail.com", "Password@123", "Pay User");
            Long catId = createCategory(token, "PayCat");
            Long productId = createProduct(token, catId, "Pay Item", BigDecimal.valueOf(200.0));
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO createOrd = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            MvcResult ordRes = mockMvc.perform(
                            post("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(createOrd))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                    .get("data").get("orderId").asLong();

            CreatePaymentRequestDTO payReq = CreatePaymentRequestDTO.builder()
                    .orderId(orderId)
                    .paymentMethod(com.ecom.shopsphere.entity.PaymentMethod.UPI)
                    .build();

            mockMvc.perform(
                            post("/api/v1/payments")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(payReq))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.orderId").value(orderId.intValue()))
                    .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));

            assertEquals(1, paymentRepository.count());
        }

        @Test
        void getPaymentByOrderId_Success() throws Exception {
            String token = registerAndLogin("payget@gmail.com", "Password@123", "Pay Get");
            Long catId = createCategory(token, "PGCat");
            Long productId = createProduct(token, catId, "PG Item", BigDecimal.TEN);
            Long addressId = createAddress(token);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(
                                    AddCartItemRequestDTO.builder()
                                            .productId(productId).quantity(1).build()
                            ))
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO createOrd = CreateOrderRequestDTO.builder()
                    .shippingAddressId(addressId).build();

            MvcResult ordRes = mockMvc.perform(
                            post("/api/v1/orders")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(createOrd))
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                    .get("data").get("orderId").asLong();

            CreatePaymentRequestDTO payReq = CreatePaymentRequestDTO.builder()
                    .orderId(orderId)
                    .paymentMethod(com.ecom.shopsphere.entity.PaymentMethod.CREDIT_CARD)
                    .build();

            mockMvc.perform(
                    post("/api/v1/payments")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(payReq))
            ).andExpect(status().isCreated());

            mockMvc.perform(
                            get("/api/v1/payments/order/" + orderId)
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.amount").value(10.0));
        }
    }

    // ======================================================
    // ADMIN CONTROLLER INTEGRATION TESTS
    // ======================================================
    @Nested
    class AdminControllerTests {

        private String registerAndLoginAsAdmin(
                String email,
                String password,
                String fullName) throws Exception {

            RegisterRequestDTO register = RegisterRequestDTO.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(password)
                    .phoneNumber("9876543210")
                    .build();

            mockMvc.perform(
                    post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(register))
            ).andExpect(status().isCreated());

            // Change the newly registered user's role to ADMIN
            User user = userRepository.findByEmail(email)
                    .orElseThrow();

            user.setRole(com.ecom.shopsphere.entity.Role.ADMIN);

            userRepository.save(user);

            LoginRequestDTO login = LoginRequestDTO.builder()
                    .email(email)
                    .password(password)
                    .build();

            MvcResult loginResult = mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(login))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").exists())
                    .andReturn();

            return jsonMapper
                    .readTree(
                            loginResult
                                    .getResponse()
                                    .getContentAsString()
                    )
                    .get("data")
                    .get("token")
                    .stringValue();
        }

        @Test
        void getDashboard_Success() throws Exception {

            String token = registerAndLoginAsAdmin(
                    "adminuser@gmail.com",
                    "Password@123",
                    "Admin User"
            );

            mockMvc.perform(
                            get("/api/v1/admin/orders/dashboard")
                                    .header("Authorization", "Bearer " + token)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalOrders").exists())
                    .andExpect(jsonPath("$.data.totalUsers").exists());
        }

        @Test
        void adminOrderLifecycle_ConfirmThroughDeliver() throws Exception {

            // =========================
            // CUSTOMER
            // =========================

            String userToken = registerAndLogin(
                    "cust@gmail.com",
                    "Password@123",
                    "Customer"
            );

            // =========================
            // CUSTOMER DATA
            // =========================

            Long catId = createCategory(
                    userToken,
                    "AdminCat"
            );

            Long productId = createProduct(
                    userToken,
                    catId,
                    "Admin Item",
                    BigDecimal.valueOf(300.0)
            );

            Long addressId = createAddress(userToken);

            mockMvc.perform(
                    post("/api/v1/cart/items")
                            .header(
                                    "Authorization",
                                    "Bearer " + userToken
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    jsonMapper.writeValueAsString(
                                            AddCartItemRequestDTO.builder()
                                                    .productId(productId)
                                                    .quantity(1)
                                                    .build()
                                    )
                            )
            ).andExpect(status().isCreated());

            CreateOrderRequestDTO createOrd =
                    CreateOrderRequestDTO.builder()
                            .shippingAddressId(addressId)
                            .build();

            MvcResult ordRes = mockMvc.perform(
                            post("/api/v1/orders")
                                    .header(
                                            "Authorization",
                                            "Bearer " + userToken
                                    )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            jsonMapper.writeValueAsString(
                                                    createOrd
                                            )
                                    )
                    )
                    .andExpect(status().isCreated())
                    .andReturn();

            Long orderId = jsonMapper
                    .readTree(
                            ordRes.getResponse()
                                    .getContentAsString()
                    )
                    .get("data")
                    .get("orderId")
                    .asLong();

            // =========================
            // ADMIN
            // =========================

            String adminToken = registerAndLoginAsAdmin(
                    "admin@gmail.com",
                    "Password@123",
                    "Admin User"
            );

            // =========================
            // ADMIN ORDER LIFECYCLE
            // =========================

            mockMvc.perform(
                            patch(
                                    "/api/v1/admin/orders/"
                                            + orderId
                                            + "/confirm"
                            )
                                    .header(
                                            "Authorization",
                                            "Bearer " + adminToken
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.orderStatus")
                                    .value("CONFIRMED")
                    );

            mockMvc.perform(
                            patch(
                                    "/api/v1/admin/orders/"
                                            + orderId
                                            + "/processing"
                            )
                                    .header(
                                            "Authorization",
                                            "Bearer " + adminToken
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.orderStatus")
                                    .value("PROCESSING")
                    );

            mockMvc.perform(
                            patch(
                                    "/api/v1/admin/orders/"
                                            + orderId
                                            + "/ship"
                            )
                                    .header(
                                            "Authorization",
                                            "Bearer " + adminToken
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.orderStatus")
                                    .value("SHIPPED")
                    );

            mockMvc.perform(
                            patch(
                                    "/api/v1/admin/orders/"
                                            + orderId
                                            + "/deliver"
                            )
                                    .header(
                                            "Authorization",
                                            "Bearer " + adminToken
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.orderStatus")
                                    .value("DELIVERED")
                    );
        }
    }
}
