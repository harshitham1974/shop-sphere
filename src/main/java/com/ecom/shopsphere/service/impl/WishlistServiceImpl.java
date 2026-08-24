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
import com.ecom.shopsphere.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    private final WishlistItemRepository wishlistItemRepository;

    private final ProductRepository productRepository;

    private final WishlistMapper wishlistMapper;

    private final CurrentUserService currentUserService;

    @Override
    public WishlistResponseDTO addToWishlist(
            AddWishlistItemRequestDTO request) {

        User user = currentUserService.getCurrentUser();

        Wishlist wishlist = wishlistRepository
                .findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new WishlistNotFoundException(
                                "Wishlist not found."));

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found."));

        WishlistItem existingWishlistItem =
                wishlistItemRepository
                        .findByWishlistWishlistIdAndProductProductId(
                                wishlist.getWishlistId(),
                                product.getProductId())
                        .orElse(null);

        if (existingWishlistItem != null) {

            log.info(
                    "Product already exists in wishlist. Product ID: {}",
                    product.getProductId());

            return wishlistMapper.toWishlistResponseDTO(wishlist);
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        wishlistItemRepository.save(wishlistItem);

        log.info(
                "Product added to wishlist successfully. Product ID: {}",
                product.getProductId());

        return wishlistMapper.toWishlistResponseDTO(wishlist);
    }

    @Override
    public WishlistResponseDTO getWishlist() {

        User user = currentUserService.getCurrentUser();

        Wishlist wishlist = wishlistRepository
                .findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new WishlistNotFoundException(
                                "Wishlist not found."));

        return wishlistMapper.toWishlistResponseDTO(wishlist);
    }

    @Override
    public DeleteWishlistResponseDTO removeWishlistItem(
            Long wishlistItemId) {

        User user = currentUserService.getCurrentUser();

        Wishlist wishlist = wishlistRepository
                .findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new WishlistNotFoundException(
                                "Wishlist not found."));

        WishlistItem wishlistItem =
                wishlistItemRepository
                        .findById(wishlistItemId)
                        .orElseThrow(() ->
                                new WishlistItemNotFoundException(
                                        "Wishlist item not found."));

        if (!wishlistItem.getWishlist()
                .getWishlistId()
                .equals(wishlist.getWishlistId())) {

            throw new WishlistItemNotFoundException(
                    "Wishlist item does not belong to the current user.");
        }

        wishlistItemRepository.delete(wishlistItem);

        log.info(
                "Wishlist item removed successfully. Wishlist Item ID: {}",
                wishlistItemId);

        return DeleteWishlistResponseDTO.builder()
                .state("REMOVED")
                .build();
    }
}