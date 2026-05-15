package com.example.nsu_backend.schedulers;

import com.example.nsu_backend.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {
    private final RefreshTokenService refreshTokenService;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    @Transactional
    public void reportCurrentTime() {
        refreshTokenService.cleanUpExpiredTokens();
        log.info("Cleaning up expired refresh tokens.");
    }
}
