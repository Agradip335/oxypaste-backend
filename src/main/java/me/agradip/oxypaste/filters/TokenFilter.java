package me.agradip.oxypaste.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class TokenFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public TokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authorizationHeader = request.getHeader("Authorization");

        // 1. Require authentication for /api/user/token/**
        if (requestURI.startsWith("/api/user/token/")) {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                return;
            }
        }

        // 2. Check for Authorization header (if present) on /api/paste/** but do not require it
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if (!tokenService.isTokenValid(token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                return;
            }

            Optional<User> userOpt = tokenService.getUserFromToken(token);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Create an Authentication object and set it in the SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, null); // Add roles if needed
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
