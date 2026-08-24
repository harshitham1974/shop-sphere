package com.ecom.shopsphere.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteReviewResponseDTO {

    private Long reviewId;

    private String state;
}
