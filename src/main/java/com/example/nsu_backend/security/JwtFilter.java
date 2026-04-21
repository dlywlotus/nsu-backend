package com.example.nsu_backend.security;

import com.example.nsu_backend.dto.GenericError;
import com.example.nsu_backend.exceptions.AccessTokenException;
import com.example.nsu_backend.properties.JwtProperties;
import com.example.nsu_backend.utils.AuthUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.nsu_backend.utils.AuthUtils.ACCESS_TOKEN_VERSION_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProperties jwtProperties;
    private final AuthUtils authUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jws = authUtils.removeBearerPrefix(token);
            Claims payload = Jwts.parser().verifyWith(jwtProperties.getDecodedSecretKey())
                    .build().parseSignedClaims(jws).getPayload();
            Object userId = payload.get("userId");
            Object accessTokenVersion = payload.get("version");
            Integer globalAccessTokenVersion = (Integer) Optional.ofNullable(
                    redisTemplate.opsForValue().get(ACCESS_TOKEN_VERSION_PREFIX + userId.toString())).orElse(1);

            //If access token is blacklisted or has an outdated version
            if (redisTemplate.hasKey("blackListedAccessToken:" + jws) ||
                    (Integer) accessTokenVersion < globalAccessTokenVersion) {
                throw new AccessTokenException("");
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | AccessTokenException e) {
            response.setStatus(401);
            response.setContentType("application/json");
            GenericError error = new GenericError("Access token has expired");
            String jsonResponseString = objectMapper.writeValueAsString(error);
            response.getWriter().write(jsonResponseString);
        } catch (JwtException e) {
            response.setStatus(401);
            response.setContentType("application/json");
            GenericError error = new GenericError("Invalid access token provided");
            String jsonResponseString = objectMapper.writeValueAsString(error);
            response.getWriter().write(jsonResponseString);
        }
    }
}