package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.dto.ResponsesDto.TokenCreatedResponse;
import me.agradip.oxypaste.dto.ResponsesDto.TokenRevokedResponse;
import me.agradip.oxypaste.dto.ResponsesDto.TokenViewResponse;
import me.agradip.oxypaste.dto.ResponsesDto.TokenValidationResponse;

import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.AuthRequired;
import me.agradip.oxypaste.service.TokenService;
import me.agradip.oxypaste.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/token")
public class UserTokenController {
    private final TokenService tokenService;
    private final UserService userService;

    public UserTokenController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    // Create a new API token
    @PostMapping("/create")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public TokenCreatedResponse createApiToken(
            Principal principal,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long duration
    ) {
        Optional<User> userOpt = userService.getUserByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Token token = tokenService.createToken(userOpt.get(), Token.TokenType.API, name, description, duration);
        return new TokenCreatedResponse(token.getToken());
    }

    // List all API tokens
    @GetMapping("/list")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public List<TokenViewResponse> listApiTokens(Principal principal) {
        Optional<User> userOpt = userService.getUserByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        List<TokenViewResponse> tokens = tokenService.getTokensForUser(userOpt.get()).stream()
                .filter(token -> token.getType() == Token.TokenType.API) // Only return API tokens
                .map(token -> new TokenViewResponse(token.getName(), token.getCreatedAt(), token.getExpiresAt()))
                .collect(Collectors.toList());

        return tokens;
    }

    // Revoke a specific API token
    @DeleteMapping("/revoke")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public ResponseEntity<Void> revokeApiToken(Principal principal, @RequestParam String token) {
        Optional<User> userOpt = userService.getUserByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean revoked = tokenService.revokeToken(userOpt.get(), token);
        if (!revoked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().build();
    }

    // Revoke all API tokens for the user
    @DeleteMapping("/revoke-all")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public ResponseEntity<Void> revokeAllApiTokens(Principal principal) {
        Optional<User> userOpt = userService.getUserByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        tokenService.revokeAllTokens(userOpt.get());
        return ResponseEntity.ok().build();
    }
}
