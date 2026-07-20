package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;

    private String message;

    private T data;
}