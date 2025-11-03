package com.nckh.yte.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Collection<String> roles, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("uid", userId.toString());
        Date now = new Date();

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return parse(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        try {
            parse(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("⚠️ JWT expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("⚠️ JWT invalid: " + e.getMessage());
        }
        return false;
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
