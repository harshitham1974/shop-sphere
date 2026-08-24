package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponseDTO {

    private Long wishlistId;

    private Integer totalItems;

    private List<WishlistItemResponseDTO> items;
}