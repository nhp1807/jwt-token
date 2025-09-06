package com.example.security.service;

import com.example.security.dto.request.AuthenticationRequest;
import com.example.security.dto.response.AuthenticationResponse;
import com.example.security.dto.request.RefreshTokenRequest;
import com.example.security.dto.request.RegisterRequest;
import com.example.security.enums.Role;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.example.security.model.RefreshToken;
import com.example.security.repository.RefreshTokenRepository;
import com.example.security.cache.AccessTokenCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository repository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccessTokenCache accessTokenCache;

    @Value("${token.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    @Transactional
    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(AuthenticationResponse.builder()
                    .message("Email already exists")
                    .build());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        repository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(refreshToken, user);

        accessTokenCache.put(user.getEmail(), accessToken);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Tài khoản này chưa thiết lập mật khẩu. Vui lòng đăng nhập bằng Google hoặc đặt mật khẩu mới.");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(refreshToken, user);

        accessTokenCache.put(user.getEmail(), accessToken);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            User userDetails = repository.findByEmail(userEmail)
                    .orElseThrow();

            // Check if refresh token exists in database
            RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));
            if (jwtService.isTokenValid(refreshToken, userDetails)
                    && storedRefreshToken.getExpiryDate().isAfter(Instant.now())) {

                // Only generate new access token, keep the same refresh token
                String accessToken = jwtService.generateAccessToken(userDetails);

                accessTokenCache.put(userDetails.getEmail(), accessToken);

                return ResponseEntity.ok(AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken) // Return the same refresh token
                        .build());
            }
        }

        throw new RuntimeException("Invalid refresh token");
    }

    @Transactional
    private void saveRefreshToken(String token, User user) {
        // Delete existing refresh token for this user first
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION/100)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        User user = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database"))
                .getUser();
        refreshTokenRepository.deleteByUserId(user.getId());
        accessTokenCache.invalidate(user.getEmail());
    }
}
