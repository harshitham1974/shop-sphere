package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.response.WishlistItemResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;
import com.ecom.shopsphere.entity.Wishlist;
import com.ecom.shopsphere.entity.WishlistItem;

public interface WishlistMapper {

    WishlistItemResponseDTO toWishlistItemResponseDTO(
            WishlistItem wishlistItem);

    WishlistResponseDTO toWishlistResponseDTO(
            Wishlist wishlist);

}