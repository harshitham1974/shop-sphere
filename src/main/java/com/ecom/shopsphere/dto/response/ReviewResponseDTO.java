package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private Long reviewId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    private Long userId;

    private String userName;

    private Long productId;

    private String productName;
}
