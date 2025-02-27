package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.TokenService;
import me.agradip.oxypaste.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Responses.ApiResponse<Responses.TokenCreatedResponse>> createApiToken(
            Principal principal,
            @RequestParam() String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long duration
    ) {
        Optional<User> userOpt = userService.getUserByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Responses.ApiResponse.failure("Unauthorized"));
        }

        Token token = tokenService.createToken(userOpt.get(), Token.TokenType.API, name, description, duration);
        return ResponseEntity.ok(Responses.ApiResponse.success(new Responses.TokenCreatedResponse(token.getToken())));
    }

    // List all API tokens
    @GetMapping("/list")
    public ResponseEntity<Responses.ApiResponse<List<Responses.TokenViewResponse>>> listApiTokens(Principal principal) {
        Optional<User> userOpt = userService.getUserByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Responses.ApiResponse.failure("Unauthorized"));
        }

        List<Responses.TokenViewResponse> tokens = tokenService.getTokensForUser(userOpt.get())
                .stream()
                .filter(token -> token.getType() == Token.TokenType.API) // Only return API tokens
                .map(token -> new Responses.TokenViewResponse(token.getName(), token.getCreatedAt(), token.getExpiresAt()))
                .toList();

        return ResponseEntity.ok(Responses.ApiResponse.success(tokens));
    }

    // Revoke a specific API token
    @DeleteMapping("/revoke")
    public ResponseEntity<Responses.ApiResponse<Void>> revokeApiToken(Principal principal, @RequestParam String token) {
        Optional<User> userOpt = userService.getUserByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Responses.ApiResponse.failure("Unauthorized"));
        }

        boolean revoked = tokenService.revokeToken(userOpt.get(), token);
        if (!revoked) {
            return ResponseEntity.status(404).body(Responses.ApiResponse.failure("Token not found or unauthorized"));
        }

        return ResponseEntity.ok(Responses.ApiResponse.success(null));
    }

    // Revoke all API tokens for the user
    @DeleteMapping("/revoke-all")
    public ResponseEntity<Responses.ApiResponse<Void>> revokeAllApiTokens(Principal principal) {
        Optional<User> userOpt = userService.getUserByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Responses.ApiResponse.failure("Unauthorized"));
        }

        tokenService.revokeAllTokens(userOpt.get());
        return ResponseEntity.ok(Responses.ApiResponse.success(null));
    }
}
