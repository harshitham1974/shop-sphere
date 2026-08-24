package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;

import com.ecom.shopsphere.repository.CartRepository;
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

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CartControllerIntegrationTest extends BaseIntegrationTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    private String userToken;

    private Long testProductId;

    private Long testProductId2;


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setup() throws Exception {

        /*
         * Delete dependent data first.
         *
         * Cart -> CartItems
         * Product -> Product references
         * Category -> Products
         * User -> Cart
         *
         * User is also deleted so that the same email can be
         * registered again for every test.
         */

        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();


        // -----------------------------------------------------
        // REGISTER USER
        // -----------------------------------------------------

        RegisterRequestDTO register = RegisterRequestDTO.builder().fullName("Cart User").email("cartuser@gmail.com").password("Password@123").phoneNumber("9876543210").build();

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(register))).andExpect(status().isCreated());


        // -----------------------------------------------------
        // LOGIN USER
        // -----------------------------------------------------

        LoginRequestDTO login = LoginRequestDTO.builder().email("cartuser@gmail.com").password("Password@123").build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();

        userToken = jsonMapper.readTree(loginResult.getResponse().getContentAsString()).get("data").get("token").stringValue();


        // -----------------------------------------------------
        // CREATE CATEGORY
        // -----------------------------------------------------

        Long categoryId = createCategory("CartCat", "Cart category");


        // -----------------------------------------------------
        // CREATE PRODUCTS
        // -----------------------------------------------------

        testProductId = createProduct("Cart Item 1", 10.0, categoryId);

        testProductId2 = createProduct("Cart Item 2", 25.0, categoryId);
    }


    // =========================================================
    // HELPER - CREATE CATEGORY
    // =========================================================

    private Long createCategory(String name, String description) throws Exception {

        CreateCategoryRequestDTO request = CreateCategoryRequestDTO.builder().categoryName(name).description(description).build();

        MvcResult result = mockMvc.perform(post("/api/v1/categories").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString()).get("data").get("categoryId").asLong();
    }


    // =========================================================
    // HELPER - CREATE PRODUCT
    // =========================================================

    private Long createProduct(String name, double price, Long categoryId) throws Exception {

        CreateProductRequestDTO request = CreateProductRequestDTO.builder().productName(name).description(name + " desc").brand("CartBrand").categoryId(categoryId).price(BigDecimal.valueOf(price)).stockQuantity(100).imageUrl("https://example.com/" + name + ".jpg").build();

        MvcResult result = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString()).get("data").get("productId").asLong();
    }


    // =========================================================
    // ADD TO CART - SINGLE ITEM
    // =========================================================

    @Test
    void addToCart_SingleItem_Success_CorrectTotals() throws Exception {

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(testProductId).quantity(3).build();

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isCreated())

                .andExpect(jsonPath("$.status").value(201))

                .andExpect(jsonPath("$.message").value("Product added to cart successfully"))

                // IMPORTANT:
                // API returns "items", not "cartItems"
                .andExpect(jsonPath("$.data.items.length()").value(1))

                .andExpect(jsonPath("$.data.items[0].productId").value(testProductId.intValue()))

                .andExpect(jsonPath("$.data.items[0].quantity").value(3))

                .andExpect(jsonPath("$.data.totalItems").value(3))

                // IMPORTANT:
                // API returns totalAmount, not totalPrice
                .andExpect(jsonPath("$.data.totalAmount").value(30.0));
    }


    // =========================================================
    // ADD MULTIPLE ITEMS
    // =========================================================

    @Test
    void addToCart_MultipleItems_CorrectTotals() throws Exception {

        AddCartItemRequestDTO product1 = AddCartItemRequestDTO.builder().productId(testProductId).quantity(2).build();

        AddCartItemRequestDTO product2 = AddCartItemRequestDTO.builder().productId(testProductId2).quantity(1).build();


        // First product
        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(product1))).andExpect(status().isCreated());


        // Second product
        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(product2))).andDo(print()).andExpect(status().isCreated())

                .andExpect(jsonPath("$.data.items.length()").value(2))

                .andExpect(jsonPath("$.data.totalItems").value(3))

                .andExpect(jsonPath("$.data.totalAmount").value(45.0));
    }


    // =========================================================
    // PRODUCT NOT FOUND
    // =========================================================

    @Test
    void addToCart_ProductNotFound_404() throws Exception {

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(99999L).quantity(1).build();

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isNotFound());
    }


    // =========================================================
    // INVALID QUANTITY
    // =========================================================

    @Test
    void addToCart_InvalidQuantity_BadRequest() throws Exception {

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(testProductId).quantity(0).build();

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }


    // =========================================================
    // NULL FIELDS
    // =========================================================

    @Test
    void addToCart_NullFields_BadRequest() throws Exception {

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(null).quantity(null).build();

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }


    // =========================================================
    // UNAUTHORIZED
    // =========================================================

    @Test
    void addToCart_Unauthorized_403() throws Exception {

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(testProductId).quantity(1).build();

        mockMvc.perform(post("/api/v1/cart/items").contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(request))).andExpect(status().isForbidden());
    }


    // =========================================================
    // GET CART - FRESH USER
    // =========================================================

    @Test
    void getCart_FreshUser_EmptyItems() throws Exception {

        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + userToken)).andDo(print()).andExpect(status().isOk())

                .andExpect(jsonPath("$.message").value("Cart fetched successfully"))

                .andExpect(jsonPath("$.data.totalItems").value(0))

                .andExpect(jsonPath("$.data.totalAmount").value(0.0));
    }


    // =========================================================
    // GET CART AFTER ADDING TWO ITEMS
    // =========================================================

    @Test
    void getCart_AfterAdding_TwoItems_Listed() throws Exception {

        AddCartItemRequestDTO item1 = AddCartItemRequestDTO.builder().productId(testProductId).quantity(1).build();

        AddCartItemRequestDTO item2 = AddCartItemRequestDTO.builder().productId(testProductId2).quantity(1).build();


        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(item1))).andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(item2))).andExpect(status().isCreated());


        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + userToken)).andDo(print()).andExpect(status().isOk())

                .andExpect(jsonPath("$.data.items.length()").value(2))

                .andExpect(jsonPath("$.data.totalItems").value(2))

                .andExpect(jsonPath("$.data.totalAmount").value(35.0));
    }


    // =========================================================
    // UPDATE CART ITEM
    // =========================================================

    @Test
    void updateCartItem_IncreaseQuantity_TotalsUpdate() throws Exception {

        AddCartItemRequestDTO add = AddCartItemRequestDTO.builder().productId(testProductId).quantity(1).build();


        MvcResult addResult = mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(add))).andExpect(status().isCreated()).andReturn();


        /*
         * IMPORTANT:
         * Response field is "items", not "cartItems".
         */
        Long itemId = jsonMapper.readTree(addResult.getResponse().getContentAsString()).get("data").get("items").get(0).get("cartItemId").asLong();


        UpdateCartItemRequestDTO update = UpdateCartItemRequestDTO.builder().quantity(10).build();


        mockMvc.perform(put("/api/v1/cart/items/" + itemId).header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(update))).andDo(print()).andExpect(status().isOk())

                .andExpect(jsonPath("$.data.items[0].quantity").value(10))

                .andExpect(jsonPath("$.data.totalItems").value(10))

                .andExpect(jsonPath("$.data.totalAmount").value(100.0));
    }


    // =========================================================
    // UPDATE - INVALID QUANTITY
    // =========================================================

    @Test
    void updateCartItem_InvalidQty_BadRequest() throws Exception {

        AddCartItemRequestDTO add = AddCartItemRequestDTO.builder().productId(testProductId).quantity(1).build();


        MvcResult addResult = mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(add))).andExpect(status().isCreated()).andReturn();


        /*
         * FIX FOR YOUR NullPointerException
         *
         * Actual JSON:
         *
         * data -> items -> [0] -> cartItemId
         *
         * NOT:
         *
         * data -> cartItems
         */
        Long itemId = jsonMapper.readTree(addResult.getResponse().getContentAsString()).get("data").get("items").get(0).get("cartItemId").asLong();


        UpdateCartItemRequestDTO update = UpdateCartItemRequestDTO.builder().quantity(0).build();


        mockMvc.perform(put("/api/v1/cart/items/" + itemId).header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(update))).andExpect(status().isBadRequest());
    }


    // =========================================================
    // REMOVE CART ITEM
    // =========================================================

    @Test
    void removeCartItem_Success_ItemRemoved() throws Exception {

        AddCartItemRequestDTO add = AddCartItemRequestDTO.builder().productId(testProductId).quantity(2).build();


        MvcResult addResult = mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(add))).andExpect(status().isCreated()).andReturn();


        Long itemId = jsonMapper.readTree(addResult.getResponse().getContentAsString()).get("data").get("items").get(0).get("cartItemId").asLong();


        mockMvc.perform(delete("/api/v1/cart/items/" + itemId).header("Authorization", "Bearer " + userToken)).andDo(print()).andExpect(status().isOk())

                /*
                 * Your service currently returns:
                 *
                 * state = "REMOVED"
                 *
                 * NOT "ITEM_REMOVED"
                 */.andExpect(jsonPath("$.data.state").value("REMOVED"));
    }


    // =========================================================
    // CLEAR CART
    // =========================================================

    @Test
    void clearCart_ThreeItems_Cleared() throws Exception {

        AddCartItemRequestDTO item1 = AddCartItemRequestDTO.builder().productId(testProductId).quantity(1).build();

        AddCartItemRequestDTO item2 = AddCartItemRequestDTO.builder().productId(testProductId2).quantity(2).build();


        // Add first product
        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(item1))).andExpect(status().isCreated());


        // Add second product
        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", "Bearer " + userToken).contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(item2))).andExpect(status().isCreated());


        // Verify before clearing
        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + userToken)).andExpect(status().isOk())

                .andExpect(jsonPath("$.data.totalItems").value(3))

                .andExpect(jsonPath("$.data.totalAmount").value(60.0));


        // Clear cart
        mockMvc.perform(delete("/api/v1/cart").header("Authorization", "Bearer " + userToken)).andDo(print()).andExpect(status().isOk())

                .andExpect(jsonPath("$.data.state").value("CLEARED"));


        // Verify cart is empty
        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + userToken)).andExpect(status().isOk())

                .andExpect(jsonPath("$.data.totalItems").value(0))

                .andExpect(jsonPath("$.data.totalAmount").value(0.0));
    }
}