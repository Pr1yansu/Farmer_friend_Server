package com.priyansu.authentication.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.authentication.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final String SECRET_KEY = "mySecret";

    public String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey("mySecret").parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public User extractUserFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        String userJson = (String) claims.get("user");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(userJson, User.class);
        } catch (JsonProcessingException e) {
            e.fillInStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String generateToken(User user) {
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson;
        try {
            userJson = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.fillInStackTrace();
            throw new RuntimeException(e);
        }
        return Jwts.builder()
                .claim("user", userJson)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }
}
