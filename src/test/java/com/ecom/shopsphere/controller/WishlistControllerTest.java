package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddWishlistItemRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteWishlistResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.WishlistItemNotFoundException;
import com.ecom.shopsphere.exception.WishlistNotFoundException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void addToWishlist_Success() throws Exception {

        AddWishlistItemRequestDTO request =
                AddWishlistItemRequestDTO.builder()
                        .productId(1L)
                        .build();

        WishlistResponseDTO response =
                WishlistResponseDTO.builder()
                        .wishlistId(1L)
                        .totalItems(1)
                        .build();

        when(wishlistService.addToWishlist(any(AddWishlistItemRequestDTO.class)))
                .thenReturn(response);


        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Product added to wishlist successfully."))
                .andExpect(jsonPath("$.data.wishlistId")
                        .value(1));

        verify(wishlistService)
                .addToWishlist(any(AddWishlistItemRequestDTO.class));
    }


    @Test
    void getWishlist_Success() throws Exception {

        WishlistResponseDTO response =
                WishlistResponseDTO.builder()
                        .wishlistId(1L)
                        .totalItems(2)
                        .build();

        when(wishlistService.getWishlist())
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/v1/wishlist")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Wishlist fetched successfully."))
                .andExpect(jsonPath("$.data.wishlistId")
                        .value(1));

        verify(wishlistService)
                .getWishlist();
    }


    @Test
    void removeWishlistItem_Success() throws Exception {

        DeleteWishlistResponseDTO response =
                DeleteWishlistResponseDTO.builder()
                        .state("REMOVED")
                        .build();

        when(wishlistService.removeWishlistItem(1L))
                .thenReturn(response);


        mockMvc.perform(
                        delete("/api/v1/wishlist/items/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Wishlist item removed successfully."))
                .andExpect(jsonPath("$.data.state")
                        .value("REMOVED"));

        verify(wishlistService)
                .removeWishlistItem(1L);
    }


    @Test
    void getWishlist_WishlistNotFoundException() throws Exception {

        when(wishlistService.getWishlist())
                .thenThrow(
                        new WishlistNotFoundException(
                                "Wishlist not found."
                        )
                );


        mockMvc.perform(
                        get("/api/v1/wishlist")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Wishlist Not Found"));

        verify(wishlistService)
                .getWishlist();
    }


    @Test
    void addToWishlist_ProductNotFoundException() throws Exception {

        AddWishlistItemRequestDTO request =
                AddWishlistItemRequestDTO.builder()
                        .productId(999L)
                        .build();

        when(wishlistService.addToWishlist(any(AddWishlistItemRequestDTO.class)))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product not found."
                        )
                );


        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product Not Found"));

        verify(wishlistService)
                .addToWishlist(any(AddWishlistItemRequestDTO.class));
    }


    @Test
    void addToWishlist_WishlistNotFoundException() throws Exception {

        AddWishlistItemRequestDTO request =
                AddWishlistItemRequestDTO.builder()
                        .productId(1L)
                        .build();

        when(wishlistService.addToWishlist(any(AddWishlistItemRequestDTO.class)))
                .thenThrow(
                        new WishlistNotFoundException(
                                "Wishlist not found."
                        )
                );


        mockMvc.perform(
                        post("/api/v1/wishlist/items")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Wishlist Not Found"));

        verify(wishlistService)
                .addToWishlist(any(AddWishlistItemRequestDTO.class));
    }


    @Test
    void removeWishlistItem_WishlistItemNotFoundException() throws Exception {

        when(wishlistService.removeWishlistItem(999L))
                .thenThrow(
                        new WishlistItemNotFoundException(
                                "Wishlist item not found."
                        )
                );


        mockMvc.perform(
                        delete("/api/v1/wishlist/items/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Wishlist Item Not Found"));

        verify(wishlistService)
                .removeWishlistItem(999L);
    }

}
