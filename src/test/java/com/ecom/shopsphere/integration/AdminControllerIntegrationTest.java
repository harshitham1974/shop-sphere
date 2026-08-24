package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.*;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentMethod;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private String userToken;
    private String adminToken;

    private Long userAddressId;
    private Long productId;

    @BeforeEach
    void setup() throws Exception {

        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // -------------------------
        // Normal USER
        // -------------------------

        RegisterRequestDTO user = RegisterRequestDTO.builder()
                .fullName("Order User")
                .email("orderuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(user))
        ).andExpect(status().isCreated());

        userToken = extractToken(
                "orderuser@gmail.com",
                "Password@123"
        );

        // -------------------------
        // ADMIN
        // -------------------------

        RegisterRequestDTO admin = RegisterRequestDTO.builder()
                .fullName("Admin User")
                .email("admin@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543211")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(admin))
        ).andExpect(status().isCreated());

        User adminUser = userRepository
                .findByEmail("admin@gmail.com")
                .orElseThrow();

        adminUser.setRole(Role.ADMIN);

        userRepository.save(adminUser);

        adminToken = extractToken(
                "admin@gmail.com",
                "Password@123"
        );

        // -------------------------
        // CATEGORY
        // -------------------------

        Long categoryId = createCategory(
                "Admin Order Category",
                "Admin Order"
        );

        // -------------------------
        // PRODUCT
        // -------------------------

        productId = createProduct(
                "Admin Order Product",
                150.0,
                categoryId
        );

        // -------------------------
        // ADDRESS
        // -------------------------

        userAddressId = createAddress(userToken);
    }

    // =========================================================
    // AUTH
    // =========================================================

    private String extractToken(
            String email,
            String password) throws Exception {

        LoginRequestDTO login = LoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(login)
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        return jsonMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("token")
                .stringValue();
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private Long createCategory(
            String name,
            String description) throws Exception {

        CreateCategoryRequestDTO request =
                CreateCategoryRequestDTO.builder()
                        .categoryName(name)
                        .description(description)
                        .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("categoryId")
                .asLong();
    }

    // =========================================================
    // PRODUCT
    // =========================================================

    private Long createProduct(
            String name,
            double price,
            Long categoryId) throws Exception {

        CreateProductRequestDTO request =
                CreateProductRequestDTO.builder()
                        .productName(name)
                        .description(name + " description")
                        .brand("AdminBrand")
                        .categoryId(categoryId)
                        .price(BigDecimal.valueOf(price))
                        .stockQuantity(100)
                        .imageUrl(
                                "https://example.com/"
                                        + name
                                        + ".jpg"
                        )
                        .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("productId")
                .asLong();
    }

    // =========================================================
    // ADDRESS
    // =========================================================

    private Long createAddress(
            String token) throws Exception {

        AddAddressRequestDTO request =
                AddAddressRequestDTO.builder()
                        .fullName("Order Address")
                        .phoneNumber("9876543210")
                        .addressLine1("Order Street")
                        .city("Chennai")
                        .state("TN")
                        .country("India")
                        .postalCode("600001")
                        .defaultAddress(true)
                        .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/addresses")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("addressId")
                .asLong();
    }

    // =========================================================
    // CART
    // =========================================================

    private void addItemToCart(
            Long productId,
            int quantity) throws Exception {

        AddCartItemRequestDTO request =
                AddCartItemRequestDTO.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .build();

        mockMvc.perform(
                post("/api/v1/cart/items")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(request)
                        )
        ).andExpect(status().isCreated());
    }

    // =========================================================
    // ORDER
    // =========================================================

    private Long createOrder() throws Exception {

        addItemToCart(productId, 2);

        CreateOrderRequestDTO request =
                CreateOrderRequestDTO.builder()
                        .shippingAddressId(userAddressId)
                        .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("data")
                .get("orderId")
                .asLong();
    }

    // =========================================================
    // GET DASHBOARD
    // =========================================================

    @Test
    void getDashboard_Admin_Success() throws Exception {

        createOrder();

        mockMvc.perform(
                        get("/api/v1/admin/orders/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status").value(200)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Dashboard fetched successfully."
                                )
                )
                .andExpect(
                        jsonPath("$.data.totalUsers").value(2)
                )
                .andExpect(
                        jsonPath("$.data.totalProducts").value(1)
                )
                .andExpect(
                        jsonPath("$.data.totalOrders").value(1)
                )
                .andExpect(
                        jsonPath("$.data.pendingOrders").value(1)
                )
                .andExpect(
                        jsonPath("$.data.confirmedOrders").value(0)
                )
                .andExpect(
                        jsonPath("$.data.processingOrders").value(0)
                )
                .andExpect(
                        jsonPath("$.data.shippedOrders").value(0)
                )
                .andExpect(
                        jsonPath("$.data.deliveredOrders").value(0)
                )
                .andExpect(
                        jsonPath("$.data.cancelledOrders").value(0)
                );
    }

    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    @Test
    void getAllOrders_Admin_Success() throws Exception {

        createOrder();

        mockMvc.perform(
                        get("/api/v1/admin/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status").value(200)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Orders fetched successfully."
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()").value(1)
                );
    }

    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    @Test
    void getOrderById_Admin_Success() throws Exception {

        Long orderId = createOrder();

        mockMvc.perform(
                        get(
                                "/api/v1/admin/orders/"
                                        + orderId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.orderId")
                                .value(orderId.intValue())
                )
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value(
                                        OrderStatus.PENDING.name()
                                )
                )
                .andExpect(
                        jsonPath("$.data.totalAmount")
                                .value(300.0)
                );
    }

    // =========================================================
    // GET ORDER BY ID - NOT FOUND
    // =========================================================

    @Test
    void getOrderById_NotFound() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/orders/99999")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Order Not Found")
                );
    }

    // =========================================================
    // CONFIRM ORDER
    // =========================================================

    @Test
    void confirmOrder_Pending_Success() throws Exception {

        Long orderId = createOrder();

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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order confirmed successfully."
                                )
                )
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value(
                                        OrderStatus.CONFIRMED.name()
                                )
                );
    }

    // =========================================================
    // CONFIRM ORDER - NOT FOUND
    // =========================================================

    @Test
    void confirmOrder_NotFound() throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/admin/orders/99999/confirm"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // PROCESS ORDER
    // =========================================================

    @Test
    void processOrder_Confirmed_Success() throws Exception {

        Long orderId = createOrder();

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
        ).andExpect(status().isOk());

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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value(
                                        OrderStatus.PROCESSING.name()
                                )
                );
    }

    // =========================================================
    // PROCESS ORDER - INVALID STATUS
    // =========================================================

    @Test
    void processOrder_Pending_InvalidStatus() throws Exception {

        Long orderId = createOrder();

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
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid Order Status"
                                )
                );
    }

    // =========================================================
    // SHIP ORDER
    // =========================================================

    @Test
    void shipOrder_Processing_Success() throws Exception {

        Long orderId = createOrder();

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
        ).andExpect(status().isOk());

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
        ).andExpect(status().isOk());

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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value(
                                        OrderStatus.SHIPPED.name()
                                )
                );
    }

    // =========================================================
    // SHIP ORDER - INVALID STATUS
    // =========================================================

    @Test
    void shipOrder_Pending_InvalidStatus() throws Exception {

        Long orderId = createOrder();

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
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid Order Status"
                                )
                );
    }

    // =========================================================
    // DELIVER ORDER
    // =========================================================

    @Test
    void deliverOrder_Shipped_Success() throws Exception {

        Long orderId = createOrder();

        // PENDING → CONFIRMED

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
        ).andExpect(status().isOk());

        // CONFIRMED → PROCESSING

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
        ).andExpect(status().isOk());

        // PROCESSING → SHIPPED

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
        ).andExpect(status().isOk());

        // SHIPPED → DELIVERED

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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.orderStatus")
                                .value(
                                        OrderStatus.DELIVERED.name()
                                )
                );
    }

    // =========================================================
    // DELIVER ORDER - INVALID STATUS
    // =========================================================

    @Test
    void deliverOrder_Pending_InvalidStatus() throws Exception {

        Long orderId = createOrder();

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
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid Order Status"
                                )
                );
    }

    // =========================================================
    // UNAUTHORIZED - DASHBOARD
    // =========================================================

    @Test
    void getDashboard_Unauthorized_403() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/orders/dashboard")
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // UNAUTHORIZED - ALL ORDERS
    // =========================================================

    @Test
    void getAllOrders_Unauthorized_403() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/orders")
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // NORMAL USER CANNOT ACCESS ADMIN
    // =========================================================

    @Test
    void getDashboard_NormalUser_Forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/orders/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // NORMAL USER CANNOT GET ALL ORDERS
    // =========================================================

    @Test
    void getAllOrders_NormalUser_Forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                )
                .andExpect(status().isForbidden());
    }
}