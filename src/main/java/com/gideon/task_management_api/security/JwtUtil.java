package com.gideon.task_management_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.gideon.task_management_api.entity.User;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + expiration; // Token valid for specified duration
        java.util.Date now = new java.util.Date(nowMillis);
        java.util.Date expiryDate = new java.util.Date(expMillis);

        // Implement JWT token generation logic here
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

    }

    public String extractUsername(String token) {
        // Implement logic to extract username from JWT token here
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Implement logic to validate the JWT token here
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();

        return username.equals(userDetails.getUsername()) && !claims.getExpiration().before(new java.util.Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}
