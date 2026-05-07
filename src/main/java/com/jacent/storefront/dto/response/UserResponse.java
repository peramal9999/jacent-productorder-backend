package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer storeId;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
