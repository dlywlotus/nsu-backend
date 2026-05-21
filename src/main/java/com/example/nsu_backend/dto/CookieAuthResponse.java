package com.example.nsu_backend.dto;

import org.springframework.http.ResponseCookie;

public record CookieAuthResponse(ResponseCookie cookie, UserAuthResponse userAuthResponse) {
}
