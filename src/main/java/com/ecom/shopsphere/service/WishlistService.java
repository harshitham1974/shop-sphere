package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.AddWishlistItemRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteWishlistResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;

public interface WishlistService {

    WishlistResponseDTO addToWishlist(
            AddWishlistItemRequestDTO request);

    WishlistResponseDTO getWishlist();

    DeleteWishlistResponseDTO removeWishlistItem(
            Long wishlistItemId);

}