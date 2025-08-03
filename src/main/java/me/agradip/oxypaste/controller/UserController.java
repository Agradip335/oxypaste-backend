package me.agradip.oxypaste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import me.agradip.oxypaste.Utils;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.AuthRequired;
import me.agradip.oxypaste.service.RecaptchaService;
import me.agradip.oxypaste.service.TokenService;
import me.agradip.oxypaste.service.UserService;
import me.agradip.oxypaste.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "Endpoints for user account creation and authentication")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    @Value("${security.session-expiry:1440}")
    private long sessionExpiry;

    @Value("${application.mail-registration}")
    private boolean mailRegistrationEnabled;

    private RecaptchaService recaptchaService;

    public UserController(UserService userService, TokenService tokenService, RecaptchaService recaptchaService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.recaptchaService = recaptchaService;
    }

    @Operation(
            summary = "Create a new user account",
            description = "Registers a new user using username, email, and password.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "User creation accepted and email sent (if configured)",
                            content = @Content(schema = @Schema(implementation = ResponsesDto.UserCreatedResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Missing required fields"),
                    @ApiResponse(responseCode = "400", description = "reCAPTCHA verification failed"),
                    @ApiResponse(responseCode = "409", description = "An account with that username or email already exists")
            }
    )
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponsesDto.UserCreateAcceptedResponse createAccount(MultipartHttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> params = request.getParameterMap();

        String username = getParam(params, "username");
        String email = getParam(params, "email");
        String password = getParam(params, "password");
        String recaptchaToken = getParam(params, "recaptcha_token");

        recaptchaService.verify(recaptchaToken);

        String creationIp = request.getRemoteAddr();

        if (username == null || email == null || password == null) {
            throw new ApiException(400, "Missing required fields");
        }
        if (userService.getUserByUsername(username).isPresent() || userService.getUserByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that username or email already exists");
        }

        if (username.equalsIgnoreCase("root") || username.equalsIgnoreCase("anonymous")) {
            throw new ApiException((HttpStatus.FORBIDDEN), "Illegal username: " + username);
        }

        String token = userService.registerUser(username, email, password, creationIp);
        response.setStatus(HttpStatus.ACCEPTED.value());
        return new ResponsesDto.UserCreateAcceptedResponse(mailRegistrationEnabled ? null : token);
    }

    @GetMapping("/verify")
    public ResponsesDto.UserCreatedResponse verifyEmail(@RequestParam("token") String token) {
        User user = userService.verifyAndRegisterFromToken(token);

        return new ResponsesDto.UserCreatedResponse(user.getId(), user.getCreatedAt());
    }


    @Operation(
            summary = "Authenticate user and generate session token",
            description = "Logs in a user using username and password, returning an authentication token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = ResponsesDto.LoginResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Missing required fields"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping(value = "/login", consumes = "multipart/form-data")
    public ResponsesDto.LoginResponse login(@RequestHeader("User-Agent") String useragent, MultipartHttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        String username = getParam(params, "username");
        String password = getParam(params, "password");
        String recaptchaToken = getParam(params, "recaptcha_token");

        recaptchaService.verify(recaptchaToken);

        if (username == null || password == null) {
            throw new ApiException(400, "Missing required fields");
        }

        Optional<User> userOpt = userService.getUserByUsername(username);

        if (!userOpt.isPresent()) throw new ApiException(404, "User not found");
        if (!userService.authenticateUser(username, password)) throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        User user = userOpt.get();

        int activeSessionCount = tokenService.countActiveTokensForUser(user.getId(), Token.TokenType.SESSION);
        if (activeSessionCount >= tokenService.getMaxActiveSessions()) {
            throw new ApiException(429, "Session limit reached.");
        }

        Token sessionToken = tokenService.createToken(
                user,
                Token.TokenType.SESSION,
                Utils.generateRandom(4),
                String.format("Created with User-Agent %s - IP Address - %s", useragent, request.getRemoteAddr()),
                sessionExpiry
        );

        return new ResponsesDto.LoginResponse(sessionToken.getToken(), sessionToken.getExpiresAt());
    }

    @Operation(
            summary = "Get current authenticated user info",
            description = "Returns the currently logged-in user's information using the session token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User data retrieved",
                            content = @Content(schema = @Schema(implementation = ResponsesDto.LoggedUserInfoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    @GetMapping("/@me")
    @AuthRequired(tokenType = Token.TokenType.SESSION)
    public ResponsesDto.LoggedUserInfoResponse getCurrentUser() {
        User user = RestUtil.getCurrentUser();

        assert user != null;
        return new ResponsesDto.LoggedUserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }


    private String getParam(Map<String, String[]> params, String key) {
        return params.containsKey(key) ? params.get(key)[0] : null;
    }
}
