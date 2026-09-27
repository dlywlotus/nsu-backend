package com.example.nsu_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenExchangeResponse(
        @JsonProperty(value = "access_token")
        String accessToken,

        @JsonProperty(value = "expires_in")
        String expiresIn,

        String scope,

        @JsonProperty(value = "token_type")
        String tokenType,

        @JsonProperty(value = "id_token")
        String idToken
) {
}