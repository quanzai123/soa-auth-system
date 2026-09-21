package com.soa.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountDto {
    private Long id;
    private String provider;
    private String providerUserId;
    private String providerEmail;
    private LocalDateTime linkedAt;
}
