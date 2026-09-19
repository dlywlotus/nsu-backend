package com.example.nsu_backend.dto;

public record TokenExchangeRequest(String code, String clientId, String clientSecret,
                                   String redirectUri, String grantType) {
}
