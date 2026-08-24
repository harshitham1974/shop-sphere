package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.AddWishlistItemRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteWishlistResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.entity.Wishlist;
import com.ecom.shopsphere.entity.WishlistItem;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.WishlistItemNotFoundException;
import com.ecom.shopsphere.exception.WishlistNotFoundException;
import com.ecom.shopsphere.mapper.WishlistMapper;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.WishlistItemRepository;
import com.ecom.shopsphere.repository.WishlistRepository;
import com.ecom.shopsphere.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Test
    void addToWishlist_Success_NewItem() {
        AddWishlistItemRequestDTO request = AddWishlistItemRequestDTO.builder()
                .productId(1L)
                .build();

        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();
        Product product = Product.builder().productId(1L).productName("Laptop").build();
        WishlistResponseDTO response = WishlistResponseDTO.builder().wishlistId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByWishlistWishlistIdAndProductProductId(1L, 1L)).thenReturn(Optional.empty());
        when(wishlistMapper.toWishlistResponseDTO(wishlist)).thenReturn(response);

        WishlistResponseDTO result = wishlistService.addToWishlist(request);

        assertNotNull(result);
        assertEquals(1L, result.getWishlistId());

        verify(wishlistItemRepository).save(any(WishlistItem.class));
        verify(wishlistMapper).toWishlistResponseDTO(wishlist);
    }

    @Test
    void addToWishlist_Success_ItemAlreadyExists_NoDuplicate() {
        AddWishlistItemRequestDTO request = AddWishlistItemRequestDTO.builder()
                .productId(1L)
                .build();

        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();
        Product product = Product.builder().productId(1L).build();
        WishlistItem existingItem = WishlistItem.builder()
                .wishlistItemId(1L)
                .wishlist(wishlist)
                .product(product)
                .build();
        WishlistResponseDTO response = WishlistResponseDTO.builder().wishlistId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByWishlistWishlistIdAndProductProductId(1L, 1L)).thenReturn(Optional.of(existingItem));
        when(wishlistMapper.toWishlistResponseDTO(wishlist)).thenReturn(response);

        WishlistResponseDTO result = wishlistService.addToWishlist(request);

        assertNotNull(result);
        assertEquals(1L, result.getWishlistId());

        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_WishlistNotFound_ThrowsException() {
        AddWishlistItemRequestDTO request = AddWishlistItemRequestDTO.builder()
                .productId(1L)
                .build();

        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        WishlistNotFoundException exception = assertThrows(WishlistNotFoundException.class,
                () -> wishlistService.addToWishlist(request));

        assertEquals("Wishlist not found.", exception.getMessage());
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void addToWishlist_ProductNotFound_ThrowsException() {
        AddWishlistItemRequestDTO request = AddWishlistItemRequestDTO.builder()
                .productId(99L)
                .build();

        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> wishlistService.addToWishlist(request));

        assertEquals("Product not found.", exception.getMessage());
        verify(wishlistItemRepository, never()).findByWishlistWishlistIdAndProductProductId(anyLong(), anyLong());
    }

    @Test
    void getWishlist_Success() {
        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();
        WishlistResponseDTO response = WishlistResponseDTO.builder().wishlistId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toWishlistResponseDTO(wishlist)).thenReturn(response);

        WishlistResponseDTO result = wishlistService.getWishlist();

        assertNotNull(result);
        assertEquals(1L, result.getWishlistId());

        verify(wishlistRepository).findByUserUserId(1L);
        verify(wishlistMapper).toWishlistResponseDTO(wishlist);
    }

    @Test
    void getWishlist_NotFound_ThrowsException() {
        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        WishlistNotFoundException exception = assertThrows(WishlistNotFoundException.class,
                () -> wishlistService.getWishlist());

        assertEquals("Wishlist not found.", exception.getMessage());
        verify(wishlistMapper, never()).toWishlistResponseDTO(any(Wishlist.class));
    }

    @Test
    void removeWishlistItem_Success() {
        Long wishlistItemId = 10L;
        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();
        Product product = Product.builder().productId(1L).build();
        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlistItemId(wishlistItemId)
                .wishlist(wishlist)
                .product(product)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findById(wishlistItemId)).thenReturn(Optional.of(wishlistItem));

        DeleteWishlistResponseDTO result = wishlistService.removeWishlistItem(wishlistItemId);

        assertNotNull(result);
        assertEquals("REMOVED", result.getState());

        verify(wishlistItemRepository).delete(wishlistItem);
    }

    @Test
    void removeWishlistItem_WishlistNotFound_ThrowsException() {
        Long wishlistItemId = 10L;
        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        WishlistNotFoundException exception = assertThrows(WishlistNotFoundException.class,
                () -> wishlistService.removeWishlistItem(wishlistItemId));

        assertEquals("Wishlist not found.", exception.getMessage());
        verify(wishlistItemRepository, never()).findById(anyLong());
    }

    @Test
    void removeWishlistItem_ItemNotFound_ThrowsException() {
        Long wishlistItemId = 99L;
        User user = User.builder().userId(1L).build();
        Wishlist wishlist = Wishlist.builder().wishlistId(1L).user(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findById(wishlistItemId)).thenReturn(Optional.empty());

        WishlistItemNotFoundException exception = assertThrows(WishlistItemNotFoundException.class,
                () -> wishlistService.removeWishlistItem(wishlistItemId));

        assertEquals("Wishlist item not found.", exception.getMessage());
        verify(wishlistItemRepository, never()).delete(any(WishlistItem.class));
    }

    @Test
    void removeWishlistItem_WrongWishlist_ThrowsException() {
        Long wishlistItemId = 10L;
        User user = User.builder().userId(1L).build();
        Wishlist userWishlist = Wishlist.builder().wishlistId(1L).user(user).build();
        Wishlist anotherWishlist = Wishlist.builder().wishlistId(2L).build();
        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlistItemId(wishlistItemId)
                .wishlist(anotherWishlist)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Optional.of(userWishlist));
        when(wishlistItemRepository.findById(wishlistItemId)).thenReturn(Optional.of(wishlistItem));

        WishlistItemNotFoundException exception = assertThrows(WishlistItemNotFoundException.class,
                () -> wishlistService.removeWishlistItem(wishlistItemId));

        assertEquals("Wishlist item does not belong to the current user.", exception.getMessage());
        verify(wishlistItemRepository, never()).delete(any(WishlistItem.class));
    }
}
