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
        if(!request.getRequestURI().startsWith("/api/user/token/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the Bearer token from the Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null && !authorizationHeader.startsWith("Bearer ")) {
            response.sendError(401);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!tokenService.isTokenValid(token)) {
            response.sendError(403);
            return;
        }

        Optional<User> userOpt = tokenService.getUserFromToken(token);
        User user = userOpt.get(); // No need to check here, bcs TokenService#getUserFromToken function does that anyway

        // Create an Authentication object and set it in the SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, null); // You can add authorities/roles here if needed
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

}
