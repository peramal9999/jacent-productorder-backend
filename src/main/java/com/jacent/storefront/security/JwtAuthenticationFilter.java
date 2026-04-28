package com.jacent.storefront.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
    private static final String NEW_ACCESS_TOKEN_HEADER = "X-New-Access-Token";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String userEmail = null;
        boolean accessTokenExpired = false;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            // Access token is expired — try refresh token before giving up
            accessTokenExpired = true;
            userEmail = e.getClaims().getSubject();
            log.debug("Access token expired for user: {}", userEmail);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (accessTokenExpired) {
            // Attempt silent refresh using X-Refresh-Token header
            final String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
            if (refreshToken == null || refreshToken.isBlank()) {
                log.debug("Access token expired and no refresh token provided for: {}", userEmail);
                writeTokenExpiredResponse(response);
                return;
            }

            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                    log.warn("Refresh token invalid or expired for: {}", userEmail);
                    writeTokenExpiredResponse(response);
                    return;
                }

                // Issue new access token and expose it in response header
                String newAccessToken = jwtService.generateToken(userDetails);
                response.setHeader(NEW_ACCESS_TOKEN_HEADER, newAccessToken);
                log.info("Auto-refreshed access token for: {}", userEmail);

                // Authenticate the request with the new token
                authenticateUser(userDetails, request);

            } catch (Exception e) {
                log.warn("Failed to refresh token for {}: {}", userEmail, e.getMessage());
                writeTokenExpiredResponse(response);
                return;
            }

        } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // Normal path — access token still valid
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                authenticateUser(userDetails, request);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void writeTokenExpiredResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Access token expired. Provide a valid X-Refresh-Token header to auto-renew."
        ));
    }
}
