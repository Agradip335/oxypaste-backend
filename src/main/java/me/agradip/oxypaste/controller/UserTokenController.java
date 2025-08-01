package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.dto.ResponsesDto.TokenCreatedResponse;
import me.agradip.oxypaste.dto.ResponsesDto.TokenViewResponse;

import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.AuthRequired;
import me.agradip.oxypaste.service.TokenService;
import me.agradip.oxypaste.service.UserService;
import me.agradip.oxypaste.util.RestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
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
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long duration
    ) {
        User user = RestUtil.getCurrentUser();

        assert user != null;
        int currentApiTokens = tokenService.countActiveTokensForUser(user.getId(), Token.TokenType.API);
        if (currentApiTokens >= tokenService.getMaxApiTokens()) {
            throw new ApiException(429, "API token limit reached. Delete an existing token to create a new one.");
        }

        if (tokenService.tokenNameExistsForUser(user, name)) {
            throw new ApiException(409, "Token name already exists. Please choose a different name.");
        }

        Token token = tokenService.createToken(user, Token.TokenType.API, name, description, duration);
        return new TokenCreatedResponse(token.getToken());
    }

    // List all API tokens
    @GetMapping("/list")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public List<TokenViewResponse> listApiTokens(Principal principal) {
        User user = RestUtil.getCurrentUser();

        List<TokenViewResponse> tokens = tokenService.getTokensForUser(user).stream()
                .filter(token -> token.getType() == Token.TokenType.API) // Only return API tokens
                .map(token -> new TokenViewResponse(token.getName(), token.getCreatedAt(), token.getExpiresAt()))
                .collect(Collectors.toList());

        return tokens;
    }

    // Revoke a specific API token
    @DeleteMapping("/revoke")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public ResponseEntity<Void> revokeApiToken(Principal principal, @RequestParam String token) {
        User user = RestUtil.getCurrentUser();

        boolean revoked = tokenService.revokeToken(user, token);
        if (!revoked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().build();
    }

    // Revoke all API tokens for the user
    @DeleteMapping("/revoke-all")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public ResponseEntity<Void> revokeAllApiTokens(Principal principal) {
        User user = RestUtil.getCurrentUser();

        tokenService.revokeAllTokens(user);
        return ResponseEntity.ok().build();
    }
}
