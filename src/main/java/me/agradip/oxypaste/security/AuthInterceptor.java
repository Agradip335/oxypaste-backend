package me.agradip.oxypaste.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AuthRequired annotation = null;
        
        if (handler instanceof HandlerMethod method) {
            annotation = method.getMethodAnnotation(AuthRequired.class);
        }

        if (annotation == null) {
            return true;
        }

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        String tokenValue = authHeader.substring(7);
        Optional<Token> tokenOpt = tokenService.getToken(tokenValue);

        if (tokenOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }

        Token userToken = tokenOpt.get();

        // Check if token is expired
        if (userToken.isExpired()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return false;
        }

        // Enforce token type
        if (annotation.strict() && userToken.getType() != annotation.tokenType()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token type for this action");
            return false;
        }

        User user = userToken.getUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return true;
    }
}
