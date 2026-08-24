package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.*;
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

class WishlistControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private WishlistItemRepository wishlistItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private String userToken;
    private Long prod1;
    private Long prod2;
    private Long prod3;

    @BeforeEach
    void setup() throws Exception {
        wishlistItemRepository.deleteAll();
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequestDTO register = RegisterRequestDTO.builder()
                .fullName("Wishlist User")
                .email("wluser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(register))
        ).andExpect(status().isCreated());

        userToken = extractToken("wluser@gmail.com", "Password@123");

        Long catId = createCategory("WLCat", "WL");
        prod1 = createProduct("WL Item 1", 99.99, catId);
        prod2 = createProduct("WL Item 2", 49.99, catId);
        prod3 = createProduct("WL Item 3", 149.99, catId);
    }

    private String extractToken(String email, String password) throws Exception {
        LoginRequestDTO login = LoginRequestDTO.builder().email(email).password(password).build();
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
                .brand("WLBrand").categoryId(catId)
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

    @Test
    void addToWishlist_SingleItem_Success() throws Exception {
        AddWishlistItemRequestDTO req = AddWishlistItemRequestDTO.builder()
                .productId(prod1).build();

        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Product added to wishlist successfully."))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(prod1.intValue()))
                .andExpect(jsonPath("$.data.items[0].productName").value("WL Item 1"));
    }

    @Test
    void addToWishlist_MultipleItems_AllSaved() throws Exception {
        AddWishlistItemRequestDTO add1 = AddWishlistItemRequestDTO.builder()
                .productId(prod1).build();
        AddWishlistItemRequestDTO add2 = AddWishlistItemRequestDTO.builder()
                .productId(prod2).build();
        AddWishlistItemRequestDTO add3 = AddWishlistItemRequestDTO.builder()
                .productId(prod3).build();

        mockMvc.perform(post("/api/v1/wishlist/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(add1))).andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/wishlist/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(add2))).andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(add3))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items.length()").value(3));
    }

    @Test
    void addToWishlist_ProductNotFound_404() throws Exception {
        AddWishlistItemRequestDTO req = AddWishlistItemRequestDTO.builder()
                .productId(99999L).build();

        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void addToWishlist_NullProductId_BadRequest() throws Exception {
        AddWishlistItemRequestDTO req = AddWishlistItemRequestDTO.builder()
                .productId(null).build();

        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void addToWishlist_Unauthorized_403() throws Exception {
        AddWishlistItemRequestDTO req = AddWishlistItemRequestDTO.builder()
                .productId(prod1).build();

        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getWishlist_FreshUser_EmptyList() throws Exception {
        mockMvc.perform(
                        get("/api/v1/wishlist")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wishlist fetched successfully."))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void getWishlist_TwoUsers_IndependentLists() throws Exception {
        String otherEmail = "wlother@gmail.com";
        RegisterRequestDTO otherReg = RegisterRequestDTO.builder()
                .fullName("Other WL").email(otherEmail)
                .password("Password@123").phoneNumber("9876543211")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(otherReg))
        ).andExpect(status().isCreated());

        String otherToken = extractToken(otherEmail, "Password@123");

        AddWishlistItemRequestDTO add1 = AddWishlistItemRequestDTO.builder()
                .productId(prod1).build();
        AddWishlistItemRequestDTO add2 = AddWishlistItemRequestDTO.builder()
                .productId(prod2).build();

        mockMvc.perform(
                post("/api/v1/wishlist/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(add1))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/wishlist/items")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(add2))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/wishlist")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(prod1.intValue()));

        mockMvc.perform(
                        get("/api/v1/wishlist")
                                .header("Authorization", "Bearer " + otherToken)
                )
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(prod2.intValue()));
    }

    @Test
    void removeWishlistItem_OneOfTwo_RemainingOne() throws Exception {
        AddWishlistItemRequestDTO add1 = AddWishlistItemRequestDTO.builder()
                .productId(prod1).build();
        AddWishlistItemRequestDTO add2 = AddWishlistItemRequestDTO.builder()
                .productId(prod2).build();

        mockMvc.perform(
                post("/api/v1/wishlist/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(add1))
        ).andExpect(status().isCreated());

        MvcResult addRes2 = mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(add2))
                )
                .andExpect(status().isCreated()).andReturn();

        Long wlItemId2 = jsonMapper.readTree(addRes2.getResponse().getContentAsString())
                .get("data").get("items").get(1).get("wishlistItemId").asLong();

        mockMvc.perform(
                        delete("/api/v1/wishlist/items/" + wlItemId2)
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("REMOVED"));

        mockMvc.perform(
                        get("/api/v1/wishlist")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(prod1.intValue()));
    }

    @Test
    void removeWishlistItem_NotFound() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/wishlist/items/99999")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
