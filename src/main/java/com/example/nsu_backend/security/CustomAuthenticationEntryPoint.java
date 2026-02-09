package com.example.nsu_backend.security;

import com.example.nsu_backend.dto.ApiResponse;
import com.example.nsu_backend.dto.ErrorDetail;
import com.example.nsu_backend.errorCodes.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorDetail errorDetail = new ErrorDetail(AuthErrorCode.NOT_AUTHORIZED.toString(), "Please log in first");
        String jsonResponseString = new ObjectMapper().writeValueAsString(new ApiResponse<>(errorDetail));
        response.getWriter().write(jsonResponseString);
    }
}
