package com.example.nsu_backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RevokedTokenDetails {
    UUID getId();

    UUID getUserId();

    OffsetDateTime getRevokedAt();
}

