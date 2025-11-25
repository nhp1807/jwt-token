package com.example.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacebookUserInfo {
    private String facebookId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private boolean emailVerified;
}

