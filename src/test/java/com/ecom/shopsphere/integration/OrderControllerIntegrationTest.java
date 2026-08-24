package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.*;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentMethod;
import com.ecom.shopsphere.repository.*;
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

class OrderControllerIntegrationTest extends BaseIntegrationTest {

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
    private Long user1AddressId;
    private Long productId;

    @BeforeEach
    void setup() throws Exception {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequestDTO user = RegisterRequestDTO.builder()
                .fullName("Order User").email("orderuser@gmail.com")
                .password("Password@123").phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(user))
        ).andExpect(status().isCreated());

        userToken = extractToken("orderuser@gmail.com", "Password@123");

        Long catId = createCategory("OrderCat", "Order");
        productId = createProduct("Order Item", 150.0, catId);
        user1AddressId = createAddress(userToken);
    }

    private String extractToken(String email, String password) throws Exception {
        LoginRequestDTO login = LoginRequestDTO.builder()
                .email(email).password(password).build();
        MvcResult res = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(login))
                )
                .andExpect(status().isOk()).andReturn();
        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("token").stringValue();
    }

    private Long createCategory(String name, String desc) throws Exception {
        CreateCategoryRequestDTO req = CreateCategoryRequestDTO.builder()
                .categoryName(name).description(desc).build();
        MvcResult res = mockMvc.perform(
                        post("/api/v1/categories")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated()).andReturn();
        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("categoryId").asLong();
    }

    private Long createProduct(String name, double price, Long catId) throws Exception {
        CreateProductRequestDTO req = CreateProductRequestDTO.builder()
                .productName(name).description(name + " desc")
                .brand("OrderBrand").categoryId(catId)
                .price(BigDecimal.valueOf(price)).stockQuantity(100)
                .imageUrl("https://example.com/" + name + ".jpg")
                .build();
        MvcResult res = mockMvc.perform(
                        post("/api/v1/products")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated()).andReturn();
        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("productId").asLong();
    }

    private Long createAddress(String token) throws Exception {
        AddAddressRequestDTO req = AddAddressRequestDTO.builder()
                .fullName("Order Addr").phoneNumber("9876543210")
                .addressLine1("Order Street").city("Chennai")
                .state("TN").country("India")
                .postalCode("600001").defaultAddress(true).build();
        MvcResult res = mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated()).andReturn();
        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("addressId").asLong();
    }

    private void addItemToCart(Long productId, int qty) throws Exception {
        AddCartItemRequestDTO req = AddCartItemRequestDTO.builder()
                .productId(productId).quantity(qty).build();
        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void createOrder_CartWithSingleItem_Success() throws Exception {
        addItemToCart(productId, 2);

        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Order placed successfully."))
                .andExpect(jsonPath("$.data.orderStatus").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.orderNumber").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(300.0))
                .andExpect(jsonPath("$.data.orderItems.length()").value(1))
                .andExpect(jsonPath("$.data.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.data.shippingAddress").exists())
                .andExpect(jsonPath("$.data.orderId").exists());

        assertEquals(1, orderRepository.count());
    }

    @Test
    void createOrder_CartWithMultipleItems_SumCorrect() throws Exception {
        Long catId = createCategory("MOC", "MoreOrderCat");
        Long p2 = createProduct("P2", 50.0, catId);
        Long p3 = createProduct("P3", 10.0, catId);

        addItemToCart(productId, 1);
        addItemToCart(p2, 2);
        addItemToCart(p3, 5);

        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderItems.length()").value(3))
                .andExpect(jsonPath("$.data.totalAmount").value(300.0));
    }

    @Test
    void createOrder_EmptyCart_BadRequest() throws Exception {
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart Empty"));

        assertEquals(0, orderRepository.count());
    }

    @Test
    void createOrder_AddressNotFound_404() throws Exception {
        addItemToCart(productId, 1);

        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(99999L).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_NullAddressId_BadRequest() throws Exception {
        addItemToCart(productId, 1);

        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(null).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_Unauthorized_403() throws Exception {
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_AfterOrder_CartCleared() throws Exception {
        addItemToCart(productId, 1);

        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(
                post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/cart")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }

    @Test
    void getMyOrders_NoOrders_EmptyList() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Orders fetched successfully."))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getMyOrders_TwoOrders_BothListed() throws Exception {
        addItemToCart(productId, 1);
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(req))).andExpect(status().isCreated());

        addItemToCart(productId, 1);
        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(req))).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getOrderById_Success() throws Exception {
        addItemToCart(productId, 2);
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        MvcResult ordRes = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated()).andReturn();

        Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                .get("data").get("orderId").asLong();

        mockMvc.perform(
                        get("/api/v1/orders/" + orderId)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId.intValue()))
                .andExpect(jsonPath("$.data.orderStatus").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.totalAmount").value(300.0));
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders/99999")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order Not Found"));
    }

    @Test
    void getMyOrders_Unauthorized_403() throws Exception {
        mockMvc.perform(get("/api/v1/orders")).andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_PendingStatus_Success() throws Exception {
        addItemToCart(productId, 1);
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .shippingAddressId(user1AddressId).build();

        MvcResult ordRes = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated()).andReturn();

        Long orderId = jsonMapper.readTree(ordRes.getResponse().getContentAsString())
                .get("data").get("orderId").asLong();

        mockMvc.perform(
                        patch("/api/v1/orders/" + orderId + "/cancel")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("CANCELLED"));
    }

    @Test
    void cancelOrder_NotFound() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/orders/99999/cancel")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isNotFound());
    }
}
