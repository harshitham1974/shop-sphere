package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.CartItem;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.CartItemNotFoundException;
import com.ecom.shopsphere.exception.CartNotFoundException;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.mapper.CartMapper;
import com.ecom.shopsphere.repository.CartItemRepository;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.service.CurrentUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CurrentUserService currentUserService;


    @Test
    void addToCart_Success() {

        // Arrange

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(1L).quantity(2).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        Product product = Product.builder().productId(1L).productName("Laptop").price(new BigDecimal("50000.00")).build();


        CartItem cartItem = CartItem.builder().cart(cart).product(product).quantity(2).priceAtAddedTime(product.getPrice()).build();


        CartResponseDTO response = CartResponseDTO.builder().build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(productRepository.findById(1L)).thenReturn(Optional.of(product));


        when(cartItemRepository.findByCartCartIdAndProductProductId(1L, 1L)).thenReturn(Optional.empty());


        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);


        when(cartMapper.toCartResponseDTO(cart)).thenReturn(response);


        // Act

        CartResponseDTO result = cartService.addToCart(request);


        // Assert

        assertNotNull(result);


        // Verify

        verify(currentUserService).getCurrentUser();


        verify(cartRepository).findByUserUserId(1L);


        verify(productRepository).findById(1L);


        verify(cartItemRepository).findByCartCartIdAndProductProductId(1L, 1L);


        verify(cartItemRepository).save(any(CartItem.class));


        verify(cartMapper).toCartResponseDTO(cart);
    }

    @Test
    void addToCart_CartNotFound() {

        // Arrange

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(1L).quantity(2).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.empty());


        // Act & Assert

        CartNotFoundException exception = assertThrows(CartNotFoundException.class, () -> cartService.addToCart(request));


        assertEquals("Cart not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();


        verify(cartRepository).findByUserUserId(1L);


        verify(productRepository, never()).findById(anyLong());


        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_ProductNotFound() {

        // Arrange

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(99L).quantity(2).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(productRepository.findById(99L)).thenReturn(Optional.empty());


        // Act & Assert

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> cartService.addToCart(request));


        assertEquals("Product not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(productRepository).findById(99L);

        verify(cartItemRepository, never()).findByCartCartIdAndProductProductId(anyLong(), anyLong());

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_IncreasesQuantity() {

        // Arrange

        AddCartItemRequestDTO request = AddCartItemRequestDTO.builder().productId(1L).quantity(3).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        Product product = Product.builder().productId(1L).productName("Laptop").price(new BigDecimal("50000.00")).build();


        CartItem existingCartItem = CartItem.builder().cart(cart).product(product).quantity(2).priceAtAddedTime(product.getPrice()).build();


        CartResponseDTO response = CartResponseDTO.builder().build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(productRepository.findById(1L)).thenReturn(Optional.of(product));


        when(cartItemRepository.findByCartCartIdAndProductProductId(1L, 1L)).thenReturn(Optional.of(existingCartItem));


        when(cartItemRepository.save(existingCartItem)).thenReturn(existingCartItem);


        when(cartMapper.toCartResponseDTO(cart)).thenReturn(response);


        // Act

        CartResponseDTO result = cartService.addToCart(request);


        // Assert

        assertNotNull(result);


        assertEquals(5, existingCartItem.getQuantity());


        // Verify

        verify(cartItemRepository).save(existingCartItem);


        verify(cartMapper).toCartResponseDTO(cart);
    }

    @Test
    void getCart_Success() {

        // Arrange

        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        CartResponseDTO response = CartResponseDTO.builder().build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartMapper.toCartResponseDTO(cart)).thenReturn(response);


        // Act

        CartResponseDTO result = cartService.getCart();


        // Assert

        assertNotNull(result);


        // Verify

        verify(currentUserService).getCurrentUser();


        verify(cartRepository).findByUserUserId(1L);


        verify(cartMapper).toCartResponseDTO(cart);
    }

    @Test
    void getCart_CartNotFound() {

        // Arrange

        User user = User.builder().userId(1L).email("test@gmail.com").build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.empty());


        // Act & Assert

        CartNotFoundException exception = assertThrows(CartNotFoundException.class, () -> cartService.getCart());


        assertEquals("Cart not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();


        verify(cartRepository).findByUserUserId(1L);


        verify(cartMapper, never()).toCartResponseDTO(any(Cart.class));
    }

    @Test
    void updateCartItem_Success() {

        // Arrange

        Long cartItemId = 10L;


        UpdateCartItemRequestDTO request = UpdateCartItemRequestDTO.builder().quantity(5).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        Product product = Product.builder().productId(1L).productName("Laptop").price(new BigDecimal("50000.00")).build();


        CartItem cartItem = CartItem.builder().cartItemId(cartItemId).cart(cart).product(product).quantity(2).priceAtAddedTime(product.getPrice()).build();


        CartResponseDTO response = CartResponseDTO.builder().build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));


        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);


        when(cartMapper.toCartResponseDTO(cart)).thenReturn(response);


        // Act

        CartResponseDTO result = cartService.updateCartItem(cartItemId, request);


        // Assert

        assertNotNull(result);

        assertEquals(5, cartItem.getQuantity());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository).save(cartItem);

        verify(cartMapper).toCartResponseDTO(cart);
    }

    @Test
    void updateCartItem_ItemNotFound() {

        // Arrange

        Long cartItemId = 99L;


        UpdateCartItemRequestDTO request = UpdateCartItemRequestDTO.builder().quantity(5).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());


        // Act & Assert

        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartService.updateCartItem(cartItemId, request));


        assertEquals("Cart item not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository, never()).save(any(CartItem.class));

        verify(cartMapper, never()).toCartResponseDTO(any(Cart.class));
    }

    @Test
    void updateCartItem_WrongCart() {

        // Arrange

        Long cartItemId = 10L;


        UpdateCartItemRequestDTO request = UpdateCartItemRequestDTO.builder().quantity(5).build();


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart userCart = Cart.builder().cartId(1L).user(user).build();


        Cart anotherCart = Cart.builder().cartId(2L).build();


        CartItem cartItem = CartItem.builder().cartItemId(cartItemId).cart(anotherCart).quantity(2).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(userCart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));


        // Act & Assert

        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartService.updateCartItem(cartItemId, request));


        assertEquals("Cart item does not belong to this cart.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository, never()).save(any(CartItem.class));

        verify(cartMapper, never()).toCartResponseDTO(any(Cart.class));
    }

    @Test
    void removeCartItem_Success() {

        // Arrange

        Long cartItemId = 10L;


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        Product product = Product.builder().productId(1L).productName("Laptop").price(new BigDecimal("50000.00")).build();


        CartItem cartItem = CartItem.builder().cartItemId(cartItemId).cart(cart).product(product).quantity(2).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));


        // Act

        DeleteCartResponseDTO result = cartService.removeCartItem(cartItemId);


        // Assert

        assertNotNull(result);

        assertEquals("REMOVED", result.getState());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeCartItem_ItemNotFound() {

        // Arrange

        Long cartItemId = 99L;


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());


        // Act & Assert

        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartService.removeCartItem(cartItemId));


        assertEquals("Cart item not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void removeCartItem_WrongCart() {

        // Arrange

        Long cartItemId = 10L;


        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart userCart = Cart.builder().cartId(1L).user(user).build();


        Cart anotherCart = Cart.builder().cartId(2L).build();


        CartItem cartItem = CartItem.builder().cartItemId(cartItemId).cart(anotherCart).quantity(2).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(userCart));


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));


        // Act & Assert

        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartService.removeCartItem(cartItemId));


        assertEquals("Cart item does not belong to this cart.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartItemRepository).findById(cartItemId);

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void clearCart_Success() {

        // Arrange

        User user = User.builder().userId(1L).email("test@gmail.com").build();


        Cart cart = Cart.builder().cartId(1L).user(user).cartItems(new java.util.ArrayList<>(java.util.List.of(CartItem.builder().cartItemId(1L).quantity(2).build(), CartItem.builder().cartItemId(2L).quantity(1).build()))).build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));


        when(cartRepository.save(cart)).thenReturn(cart);


        // Act

        DeleteCartResponseDTO result = cartService.clearCart();


        // Assert

        assertNotNull(result);

        assertEquals("CLEARED", result.getState());

        assertTrue(cart.getCartItems().isEmpty());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartRepository).save(cart);
    }

    @Test
    void clearCart_CartNotFound() {

        // Arrange

        User user = User.builder().userId(1L).email("test@gmail.com").build();


        when(currentUserService.getCurrentUser()).thenReturn(user);


        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.empty());


        // Act & Assert

        CartNotFoundException exception = assertThrows(CartNotFoundException.class, () -> cartService.clearCart());


        assertEquals("Cart not found.", exception.getMessage());


        // Verify

        verify(currentUserService).getCurrentUser();

        verify(cartRepository).findByUserUserId(1L);

        verify(cartRepository, never()).save(any(Cart.class));
    }
}