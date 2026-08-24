package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.CreateOrderRequestDTO;
import com.ecom.shopsphere.dto.request.CreatePaymentRequestDTO;
import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentMethod;
import com.ecom.shopsphere.entity.PaymentStatus;
import com.ecom.shopsphere.repository.AddressRepository;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.CategoryRepository;
import com.ecom.shopsphere.repository.OrderRepository;
import com.ecom.shopsphere.repository.PaymentRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

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

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .fullName("Payment User")
                .email("paymentuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        userToken = extractToken("paymentuser@gmail.com", "Password@123");
    }

    private String extractToken(String email, String password) throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .stringValue();
    }

    private Long createCategory() throws Exception {
        CreateCategoryRequestDTO request = CreateCategoryRequestDTO.builder()
                .categoryName("Payments")
                .description("Payment category")
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/categories")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("categoryId")
                .asLong();
    }

    private Long createProduct(Long categoryId) throws Exception {
        CreateProductRequestDTO request = CreateProductRequestDTO.builder()
                .productName("Payment Product")
                .description("Payment Product Description")
                .brand("ShopSphere")
                .categoryId(categoryId)
                .price(BigDecimal.valueOf(249.99))
                .stockQuantity(50)
                .imageUrl("https://example.com/payment-product.jpg")
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/products")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("productId")
                .asLong();
    }

    private Long createAddress() throws Exception {
        AddAddressRequestDTO request = AddAddressRequestDTO.builder()
                .fullName("Payment User")
                .phoneNumber("9876543210")
                .addressLine1("123 Billing Street")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .defaultAddress(true)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/addresses")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("addressId")
                .asLong();
    }

    private void addItemToCart(Long productId) throws Exception {
        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder()
                .productId(productId)
                .quantity(2)
                .build();

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    private Long createOrder() throws Exception {
        Long categoryId = createCategory();
        Long productId = createProduct(categoryId);
        Long addressId = createAddress();
        addItemToCart(productId);

        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shippingAddressId(addressId)
                .build();

        MvcResult result = mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("orderId")
                .asLong();
    }

    @Test
    void createPayment_Success() throws Exception {
        Long orderId = createOrder();
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Payment completed successfully."))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.paymentMethod").value("UPI"))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transactionId").exists());

        assertEquals(1, paymentRepository.count());
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(orderId).orElseThrow().getOrderStatus());
        assertEquals(PaymentStatus.SUCCESS, orderRepository.findById(orderId).orElseThrow().getPaymentStatus());
    }

    @Test
    void createPayment_CodPayment_Fails() throws Exception {
        Long orderId = createOrder();
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.COD)
                .build();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment Failed"));

        assertEquals(1, paymentRepository.count());
        assertEquals(PaymentStatus.FAILED, paymentRepository.findByOrderOrderId(orderId).orElseThrow().getPaymentStatus());
    }

    @Test
    void createPayment_OrderNotFound() throws Exception {
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(99999L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order Not Found"));
    }

    @Test
    void createPayment_MissingPaymentMethod_BadRequest() throws Exception {
        Long orderId = createOrder();
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(orderId)
                .paymentMethod(null)
                .build();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.data.paymentMethod").value("Payment method is required."));
    }

    @Test
    void getPaymentByOrder_Success() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        CreatePaymentRequestDTO.builder()
                                                .orderId(orderId)
                                                .paymentMethod(PaymentMethod.DEBIT_CARD)
                                                .build()
                                ))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/payments/order/{orderId}", orderId)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Payment fetched successfully."))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.paymentMethod").value("DEBIT_CARD"))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));
    }

    @Test
    void getPaymentByOrder_NotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/payments/order/99999")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment Not Found"));
    }

    @Test
    void createPayment_WithoutToken_Forbidden() throws Exception {
        Long orderId = createOrder();
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        mockMvc.perform(
                        post("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void createPayment_AlreadyPaidOrder_Fails() throws Exception {

        Long orderId = createOrder();

        CreatePaymentRequestDTO request =
                CreatePaymentRequestDTO.builder()
                        .orderId(orderId)
                        .paymentMethod(PaymentMethod.UPI)
                        .build();

        // First payment
        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        // Second payment attempt
        mockMvc.perform(
                        post("/api/v1/payments")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Payment Failed"));

        // Still only ONE payment
        assertEquals(1, paymentRepository.count());
    }
}
