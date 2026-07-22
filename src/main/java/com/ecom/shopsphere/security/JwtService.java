package com.ecom.shopsphere.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private static final String SECRET =
            "shopsphere-secret-key-shopsphere-secret-key-2026";

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION =
            1000 * 60 * 60;

    public String generateToken(String email) {

        log.info("Generating JWT token for email: {}", email);

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {

        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String email = extractEmail(token);

        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public Date extractExpiration(String token) {

        return extractClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token) {

        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
}