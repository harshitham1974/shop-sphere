package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.response.WishlistItemResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;
import com.ecom.shopsphere.entity.Wishlist;
import com.ecom.shopsphere.entity.WishlistItem;
import com.ecom.shopsphere.mapper.WishlistMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WishlistMapperImpl implements WishlistMapper {

    @Override
    public WishlistItemResponseDTO toWishlistItemResponseDTO(
            WishlistItem wishlistItem) {

        return WishlistItemResponseDTO.builder()
                .wishlistItemId(wishlistItem.getWishlistItemId())
                .productId(wishlistItem.getProduct().getProductId())
                .productName(wishlistItem.getProduct().getProductName())
                .description(wishlistItem.getProduct().getDescription())
                .brand(wishlistItem.getProduct().getBrand())
                .price(wishlistItem.getProduct().getPrice())
                .imageUrl(wishlistItem.getProduct().getImageUrl())
                .categoryName(
                        wishlistItem.getProduct()
                                .getCategory()
                                .getCategoryName())
                .inStock(
                        wishlistItem.getProduct()
                                .getStockQuantity() > 0)
                .build();
    }

    @Override
    public WishlistResponseDTO toWishlistResponseDTO(
            Wishlist wishlist) {

        List<WishlistItemResponseDTO> items =
                wishlist.getWishlistItems()
                        .stream()
                        .map(this::toWishlistItemResponseDTO)
                        .toList();

        return WishlistResponseDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .totalItems(items.size())
                .items(items)
                .build();
    }
}