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

        if (hasAuthHeader) {
            String tokenValue = authHeader.substring(7).trim();

            if (tokenValue.isEmpty()) {
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

            // Type enforcement logic
            if (annotation != null) {
                Token.TokenType expectedType = annotation.tokenType();
                boolean bypassAllowed = annotation.bypassCheckIfSessionTokenProvided();

                if (userToken.getType() != expectedType) {
                    if (bypassAllowed && userToken.getType() == Token.TokenType.SESSION) {
                        // Allow session token as fallback
                    } else {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Invalid token type for this action");
                    }
                }
            }

            // Set authenticated user
            setUserAuthentication(userToken.getUser());
            return true;
        }

        // Authorization header missing and authentication is required
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
