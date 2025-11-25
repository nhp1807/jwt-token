package com.example.security.service;

import com.example.security.dto.request.FacebookAuthRequest;
import com.example.security.dto.response.AuthenticationResponse;
import com.example.security.dto.response.FacebookUserInfo;
import com.example.security.enums.Role;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.example.security.repository.RefreshTokenRepository;
import com.example.security.model.RefreshToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacebookAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${facebook.oauth2.app-id}")
    private String facebookAppId;
    
    @Value("${facebook.oauth2.app-secret}")
    private String facebookAppSecret;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String FACEBOOK_USER_INFO_URL = "https://graph.facebook.com/me?fields=id,name,email,first_name,last_name,picture";
    private static final String FACEBOOK_TOKEN_VERIFY_URL = "https://graph.facebook.com/debug_token";
    
    @Transactional
    public AuthenticationResponse authenticateWithFacebook(FacebookAuthRequest request) {
        try {
            // 1. Validate Facebook access token and get user info
            FacebookUserInfo facebookUserInfo = validateFacebookToken(request.getAccessToken());

            // 2. Find or create user
            User user = findOrCreateUser(facebookUserInfo);

            // 3. Xóa refresh token cũ của user
            refreshTokenRepository.deleteByUserId(user.getId());

            // 4. Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // 5. Lưu refresh token mới vào database
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
            refreshTokenRepository.save(refreshTokenEntity);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Facebook authentication failed: " + e.getMessage());
        }
    }
    
    private FacebookUserInfo validateFacebookToken(String accessToken) {
        try {
            // First, verify the token is valid
            verifyFacebookToken(accessToken);
            
            // Then, get user info from Facebook Graph API
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = FACEBOOK_USER_INFO_URL + "&access_token=" + accessToken;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            // Parse the response
            JsonNode userInfo = objectMapper.readTree(response.getBody());
            
            // Extract profile picture URL
            String profilePicture = null;
            if (userInfo.has("picture") && userInfo.get("picture").has("data")) {
                profilePicture = userInfo.get("picture").get("data").get("url").asText();
            }
            
            return FacebookUserInfo.builder()
                    .facebookId(userInfo.get("id").asText())
                    .email(userInfo.has("email") ? userInfo.get("email").asText() : null)
                    .firstName(userInfo.has("first_name") ? userInfo.get("first_name").asText() : null)
                    .lastName(userInfo.has("last_name") ? userInfo.get("last_name").asText() : null)
                    .profilePicture(profilePicture)
                    .emailVerified(userInfo.has("email") && !userInfo.get("email").isNull())
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate Facebook token: " + e.getMessage());
        }
    }
    
    private void verifyFacebookToken(String accessToken) {
        try {
            String url = FACEBOOK_TOKEN_VERIFY_URL + "?input_token=" + accessToken + 
                        "&access_token=" + facebookAppId + "|" + facebookAppSecret;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            JsonNode result = objectMapper.readTree(response.getBody());
            
            if (!result.has("data") || !result.get("data").has("is_valid") || 
                !result.get("data").get("is_valid").asBoolean()) {
                throw new RuntimeException("Invalid Facebook access token");
            }
            
            // Verify app ID matches
            if (result.get("data").has("app_id") && 
                !result.get("data").get("app_id").asText().equals(facebookAppId)) {
                throw new RuntimeException("Facebook token app ID does not match");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Facebook token: " + e.getMessage());
        }
    }
    
    private User findOrCreateUser(FacebookUserInfo facebookUserInfo) {
        // First, try to find user by Facebook ID
        Optional<User> existingUser = userRepository.findByFacebookId(facebookUserInfo.getFacebookId());
        
        if (existingUser.isPresent()) {
            // User already exists with this Facebook ID
            return existingUser.get();
        }
        
        // Try to find user by email
        if (facebookUserInfo.getEmail() != null) {
            Optional<User> userByEmail = userRepository.findByEmail(facebookUserInfo.getEmail());
            
            if (userByEmail.isPresent()) {
                // User exists but doesn't have Facebook ID - link the accounts
                User user = userByEmail.get();
                user.setFacebookId(facebookUserInfo.getFacebookId());
                user.setProvider("FACEBOOK");
                if (facebookUserInfo.getProfilePicture() != null) {
                    user.setProfilePicture(facebookUserInfo.getProfilePicture());
                }
                user.setEmailVerified(facebookUserInfo.isEmailVerified());
                return userRepository.save(user);
            }
        }
        
        // Create new user
        User newUser = User.builder()
                .facebookId(facebookUserInfo.getFacebookId())
                .email(facebookUserInfo.getEmail())
                .firstName(facebookUserInfo.getFirstName())
                .lastName(facebookUserInfo.getLastName())
                .profilePicture(facebookUserInfo.getProfilePicture())
                .emailVerified(facebookUserInfo.isEmailVerified())
                .provider("FACEBOOK")
                .role(Role.USER)
                .build();
        
        return userRepository.save(newUser);
    }
}

