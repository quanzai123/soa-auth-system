package com.soa.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String accountType; // STUDENT hoặc PERSONAL
    private String role;
    private boolean hasPassword;
    private List<LinkedAccountDto> linkedAccounts;
}
