package me.agradip.oxypaste.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.TokenService;
import org.springframework.http.HttpStatus;
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

        String authHeader = request.getHeader("Authorization");
        boolean hasAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");

        // If Authorization header is present, validate it
        if (hasAuthHeader) {
            String tokenValue = authHeader.substring(7);

            if(tokenValue.isEmpty()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }

            Optional<Token> tokenOpt = tokenService.getToken(tokenValue);

            if (tokenOpt.isEmpty()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }

            Token userToken = tokenOpt.get();

            if (userToken.isExpired()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token has expired");
            }

            if (annotation != null && userToken.getType() != annotation.tokenType()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Invalid token type for this action");
            }

            // Set user authentication
            setUserAuthentication(userToken.getUser());
            return true;
        }

        // If no Authorization header and endpoint requires strict authentication
        if (annotation != null && annotation.strict()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return true;
    }

    private void setUserAuthentication(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
