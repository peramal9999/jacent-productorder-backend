package com.jacent.storefront.security;

import com.jacent.storefront.configuration.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    // ── Access token ──────────────────────────────────────────────────────────

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtConfig.getAuthExpirationMs(), getAuthSigningKey());
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtConfig.getAuthExpirationMs(), getAuthSigningKey());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token, getAuthSigningKey());
    }

    public String extractUsername(String token) {
        return extractClaim(token, getAuthSigningKey(), Claims::getSubject);
    }

    // ── Refresh token ─────────────────────────────────────────────────────────

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtConfig.getRefreshExpirationMs(), getRefreshSigningKey());
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractRefreshTokenUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token, getRefreshSigningKey());
    }

    public String extractRefreshTokenUsername(String token) {
        return extractClaim(token, getRefreshSigningKey(), Claims::getSubject);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails,
                              long expirationMs, SecretKey key) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    private boolean isTokenExpired(String token, SecretKey key) {
        return extractClaim(token, key, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return extractClaim(token, getAuthSigningKey(), claimsResolver);
    }

    private <T> T extractClaim(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token, key));
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAuthSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getAuthSecret()));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getRefreshSecret()));
    }
}
