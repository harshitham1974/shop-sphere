package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.CartItemResponseDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;
import com.ecom.shopsphere.exception.CartItemNotFoundException;
import com.ecom.shopsphere.exception.CartNotFoundException;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import com.ecom.shopsphere.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void addToCart_Success() throws Exception {

        AddCartItemRequestDTO request =
                AddCartItemRequestDTO.builder()
                        .productId(1L)
                        .quantity(2)
                        .build();

        CartItemResponseDTO cartItem =
                CartItemResponseDTO.builder()
                        .cartItemId(1L)
                        .productId(1L)
                        .productName("Test Product")
                        .imageUrl("http://example.com/image.jpg")
                        .quantity(2)
                        .unitPrice(new BigDecimal("99.99"))
                        .totalPrice(new BigDecimal("199.98"))
                        .build();

        CartResponseDTO response =
                CartResponseDTO.builder()
                        .cartId(1L)
                        .items(List.of(cartItem))
                        .totalItems(2)
                        .totalAmount(new BigDecimal("199.98"))
                        .build();

        when(cartService.addToCart(any(AddCartItemRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Product added to cart successfully"))
                .andExpect(jsonPath("$.data.cartId")
                        .value(1))
                .andExpect(jsonPath("$.data.totalItems")
                        .value(2))
                .andExpect(jsonPath("$.data.totalAmount")
                        .value(199.98))
                .andExpect(jsonPath("$.data.items[0].cartItemId")
                        .value(1))
                .andExpect(jsonPath("$.data.items[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.data.items[0].productName")
                        .value("Test Product"))
                .andExpect(jsonPath("$.data.items[0].quantity")
                        .value(2));

        verify(cartService)
                .addToCart(any(AddCartItemRequestDTO.class));
    }

    @Test
    void getCart_Success() throws Exception {

        CartItemResponseDTO cartItem =
                CartItemResponseDTO.builder()
                        .cartItemId(1L)
                        .productId(1L)
                        .productName("Test Product")
                        .imageUrl("http://example.com/image.jpg")
                        .quantity(2)
                        .unitPrice(new BigDecimal("99.99"))
                        .totalPrice(new BigDecimal("199.98"))
                        .build();

        CartResponseDTO response =
                CartResponseDTO.builder()
                        .cartId(1L)
                        .items(List.of(cartItem))
                        .totalItems(2)
                        .totalAmount(new BigDecimal("199.98"))
                        .build();

        when(cartService.getCart())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/cart")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Cart fetched successfully"))
                .andExpect(jsonPath("$.data.cartId")
                        .value(1))
                .andExpect(jsonPath("$.data.totalItems")
                        .value(2))
                .andExpect(jsonPath("$.data.totalAmount")
                        .value(199.98));

        verify(cartService)
                .getCart();
    }

    @Test
    void updateCartItem_Success() throws Exception {

        UpdateCartItemRequestDTO request =
                UpdateCartItemRequestDTO.builder()
                        .quantity(5)
                        .build();

        CartItemResponseDTO cartItem =
                CartItemResponseDTO.builder()
                        .cartItemId(1L)
                        .productId(1L)
                        .productName("Test Product")
                        .imageUrl("http://example.com/image.jpg")
                        .quantity(5)
                        .unitPrice(new BigDecimal("99.99"))
                        .totalPrice(new BigDecimal("499.95"))
                        .build();

        CartResponseDTO response =
                CartResponseDTO.builder()
                        .cartId(1L)
                        .items(List.of(cartItem))
                        .totalItems(5)
                        .totalAmount(new BigDecimal("499.95"))
                        .build();

        when(cartService.updateCartItem(eq(1L), any(UpdateCartItemRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/cart/items/1")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Cart updated successfully"))
                .andExpect(jsonPath("$.data.cartId")
                        .value(1))
                .andExpect(jsonPath("$.data.totalItems")
                        .value(5))
                .andExpect(jsonPath("$.data.items[0].quantity")
                        .value(5));

        verify(cartService)
                .updateCartItem(eq(1L), any(UpdateCartItemRequestDTO.class));
    }

    @Test
    void removeCartItem_Success() throws Exception {

        DeleteCartResponseDTO response =
                DeleteCartResponseDTO.builder()
                        .state("DELETED")
                        .build();

        when(cartService.removeCartItem(1L))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/cart/items/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Cart item removed successfully"))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(cartService)
                .removeCartItem(1L);
    }

    @Test
    void clearCart_Success() throws Exception {

        DeleteCartResponseDTO response =
                DeleteCartResponseDTO.builder()
                        .state("CLEARED")
                        .build();

        when(cartService.clearCart())
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/cart")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Cart cleared successfully"))
                .andExpect(jsonPath("$.data.state")
                        .value("CLEARED"));

        verify(cartService)
                .clearCart();
    }

    @Test
    void getCart_CartNotFoundException() throws Exception {

        when(cartService.getCart())
                .thenThrow(
                        new CartNotFoundException(
                                "Cart not found for user"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/cart")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart Not Found"));

        verify(cartService)
                .getCart();
    }

    @Test
    void updateCartItem_CartItemNotFoundException() throws Exception {

        UpdateCartItemRequestDTO request =
                UpdateCartItemRequestDTO.builder()
                        .quantity(3)
                        .build();

        when(cartService.updateCartItem(eq(999L), any(UpdateCartItemRequestDTO.class)))
                .thenThrow(
                        new CartItemNotFoundException(
                                "Cart item not found"
                        )
                );

        mockMvc.perform(
                        put("/api/v1/cart/items/999")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart Item Not Found"));

        verify(cartService)
                .updateCartItem(eq(999L), any(UpdateCartItemRequestDTO.class));
    }

    @Test
    void removeCartItem_CartItemNotFoundException() throws Exception {

        when(cartService.removeCartItem(999L))
                .thenThrow(
                        new CartItemNotFoundException(
                                "Cart item not found"
                        )
                );

        mockMvc.perform(
                        delete("/api/v1/cart/items/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart Item Not Found"));

        verify(cartService)
                .removeCartItem(999L);
    }

    @Test
    void addToCart_ProductNotFoundException() throws Exception {

        AddCartItemRequestDTO request =
                AddCartItemRequestDTO.builder()
                        .productId(999L)
                        .quantity(1)
                        .build();

        when(cartService.addToCart(any(AddCartItemRequestDTO.class)))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product not found"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/cart/items")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product Not Found"));

        verify(cartService)
                .addToCart(any(AddCartItemRequestDTO.class));
    }

    @Test
    void clearCart_CartNotFoundException() throws Exception {

        when(cartService.clearCart())
                .thenThrow(
                        new CartNotFoundException(
                                "Cart not found for user"
                        )
                );

        mockMvc.perform(
                        delete("/api/v1/cart")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart Not Found"));

        verify(cartService)
                .clearCart();
    }
}
