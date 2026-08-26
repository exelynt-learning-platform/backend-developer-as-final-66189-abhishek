package com.example.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expirationMs) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.expirationMs = expirationMs;
    }

    /**
     * Generate JWT token for authenticated user.
     */
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder().subject(userDetails.getUsername()).claim("role", userDetails.getAuthorities().iterator().next().getAuthority()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(key).compact();
    }

    /**
     * Extract username from JWT token.
     */
    public String extractUsername(String token) {

        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * Validate JWT token.
     * <p>
     * Token is valid when:
     * 1. Username inside JWT matches the authenticated user.
     * 2. Token expiration time is still in the future.
     * 3. JWT signature/parsing is valid.
     */
    public boolean isValid(String token, UserDetails userDetails) {

        try {

            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            String username = claims.getSubject();

            Date expiration = claims.getExpiration();

            return username.equals(userDetails.getUsername()) && expiration.after(new Date());

        } catch (Exception exception) {

            return false;
        }
    }
}