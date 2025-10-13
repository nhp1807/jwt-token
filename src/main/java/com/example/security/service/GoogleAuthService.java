package com.example.security.service;

import com.example.security.dto.request.GoogleAuthRequest;
import com.example.security.dto.response.AuthenticationResponse;
import com.example.security.dto.response.GoogleUserInfo;
import com.example.security.enums.Role;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.example.security.repository.RefreshTokenRepository;
import com.example.security.model.RefreshToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${google.oauth2.client-id}")
    private String googleClientId;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    
    @Transactional
    public AuthenticationResponse authenticateWithGoogle(GoogleAuthRequest request) {
        try {
            // 1. Validate Google ID token and get user info
            GoogleUserInfo googleUserInfo = validateGoogleToken(request.getIdToken());

            // 2. Find or create user
            User user = findOrCreateUser(googleUserInfo);

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
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
    
    private GoogleUserInfo validateGoogleToken(String idToken) {
        try {
            // Method 1: Use Google API client library for proper JWT validation
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                Payload payload = googleIdToken.getPayload();
                
                return GoogleUserInfo.builder()
                        .googleId(payload.getSubject())
                        .email(payload.getEmail())
                        .firstName((String) payload.get("given_name"))
                        .lastName((String) payload.get("family_name"))
                        .profilePicture((String) payload.get("picture"))
                        .emailVerified(payload.getEmailVerified())
                        .build();
            } else {
                throw new RuntimeException("Invalid Google ID token");
            }
            
        } catch (Exception e) {
            // Fallback method: Use Google userinfo endpoint
            try {
                return validateGoogleTokenViaUserInfo(idToken);
            } catch (Exception fallbackException) {
                throw new RuntimeException("Failed to validate Google token: " + e.getMessage() + " | Fallback failed: " + fallbackException.getMessage());
            }
        }
    }
    
    private GoogleUserInfo validateGoogleTokenViaUserInfo(String idToken) {
        try {
            // Create headers with the ID token as Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + idToken);
            
            // Make request to Google user info endpoint
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_USER_INFO_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            // Parse the response
            JsonNode userInfo = objectMapper.readTree(response.getBody());
            
            return GoogleUserInfo.builder()
                    .googleId(userInfo.get("sub").asText())
                    .email(userInfo.get("email").asText())
                    .firstName(userInfo.get("given_name").asText())
                    .lastName(userInfo.get("family_name").asText())
                    .profilePicture(userInfo.get("picture").asText())
                    .emailVerified(userInfo.get("email_verified").asBoolean())
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate Google token via userinfo endpoint: " + e.getMessage());
        }
    }
    
    private User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        // First, try to find user by Google ID
        Optional<User> existingUser = userRepository.findByGoogleId(googleUserInfo.getGoogleId());
        
        if (existingUser.isPresent()) {
            // User already exists with this Google ID
            return existingUser.get();
        }
        
        // Try to find user by email
        Optional<User> userByEmail = userRepository.findByEmail(googleUserInfo.getEmail());
        
        if (userByEmail.isPresent()) {
            // User exists but doesn't have Google ID - link the accounts
            User user = userByEmail.get();
            user.setGoogleId(googleUserInfo.getGoogleId());
            user.setProvider("GOOGLE");
            user.setProfilePicture(googleUserInfo.getProfilePicture());
            user.setEmailVerified(googleUserInfo.isEmailVerified());
            return userRepository.save(user);
        }
        
        // Create new user
        User newUser = User.builder()
                .googleId(googleUserInfo.getGoogleId())
                .email(googleUserInfo.getEmail())
                .firstName(googleUserInfo.getFirstName())
                .lastName(googleUserInfo.getLastName())
                .profilePicture(googleUserInfo.getProfilePicture())
                .emailVerified(googleUserInfo.isEmailVerified())
                .provider("GOOGLE")
                .role(Role.BRAND)
                .build();
        
        return userRepository.save(newUser);
    }
} 