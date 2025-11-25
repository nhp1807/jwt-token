package com.example.security.controller;

import com.example.security.dto.request.AuthenticationRequest;
import com.example.security.dto.request.RefreshTokenRequest;
import com.example.security.dto.request.RegisterRequest;
import com.example.security.dto.request.GoogleAuthRequest;
import com.example.security.dto.request.FacebookAuthRequest;
import com.example.security.dto.response.AuthenticationResponse;
import com.example.security.service.AuthenticationService;
import com.example.security.service.GoogleAuthService;
import com.example.security.service.FacebookAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {
    @Autowired
    private AuthenticationService service;
    
    @Autowired
    private GoogleAuthService googleAuthService;
    
    @Autowired
    private FacebookAuthService facebookAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return service.register(request);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return service.authenticate(request);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> authenticateWithGoogle(
            @RequestBody GoogleAuthRequest request
    ) {
        AuthenticationResponse response = googleAuthService.authenticateWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/facebook")
    public ResponseEntity<AuthenticationResponse> authenticateWithFacebook(
            @RequestBody FacebookAuthRequest request
    ) {
        AuthenticationResponse response = facebookAuthService.authenticateWithFacebook(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        return service.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody RefreshTokenRequest request
    ) {
        service.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
