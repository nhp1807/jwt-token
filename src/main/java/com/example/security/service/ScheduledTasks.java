package com.example.security.service;

import com.example.security.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredRefreshTokens() {
        refreshTokenRepository.deleteExpiredTokens();
        System.out.println("Expired refresh tokens cleaned up");
    }
} 