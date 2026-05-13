package com.example.nsu_backend.security;

import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.exceptions.AccessTokenException;
import com.example.nsu_backend.services.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authService.validateJwt(authorizationHeader.substring(7));
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | AccessTokenException e) {
            response.setStatus(401);
            response.setContentType("application/json");
            MessageResponse error = new MessageResponse("Access token has expired");
            String jsonResponseString = objectMapper.writeValueAsString(error);
            response.getWriter().write(jsonResponseString);
        } catch (JwtException e) {
            response.setStatus(401);
            response.setContentType("application/json");
            MessageResponse error = new MessageResponse("Invalid access token provided");
            String jsonResponseString = objectMapper.writeValueAsString(error);
            response.getWriter().write(jsonResponseString);
        }
    }
}