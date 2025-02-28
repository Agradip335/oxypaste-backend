package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.Utils;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.TokenService;
import me.agradip.oxypaste.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ResponsesDto.ApiResponse<ResponsesDto.UserCreatedResponse>> createAccount(MultipartHttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        String username = getParam(params, "username");
        String email = getParam(params, "email");
        String password = getParam(params, "password");
        String creationIp = request.getRemoteAddr();

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(ResponsesDto.ApiResponse.failure("Missing required fields"));
        }
        if (userService.getUserByUsername(username).isPresent() || userService.getUserByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponsesDto.ApiResponse.failure("An account with that username or email already exists"));
        }

        User user = userService.registerUser(username, email, password, creationIp);
        return ResponseEntity.ok(ResponsesDto.ApiResponse.success(new ResponsesDto.UserCreatedResponse(user.getId(), user.getCreatedAt())));
    }

    @PostMapping(value = "/login", consumes = "multipart/form-data")
    public ResponseEntity<ResponsesDto.ApiResponse<ResponsesDto.LoginResponse>> login(@RequestHeader("User-Agent") String useragent, MultipartHttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        String username = getParam(params, "username");
        String password = getParam(params, "password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(ResponsesDto.ApiResponse.failure("Missing required fields"));
        }

        Optional<User> userOpt = userService.getUserByUsername(username);

        if(!userOpt.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponsesDto.ApiResponse.failure("User not found"));
        if (!userService.authenticateUser(username, password)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponsesDto.ApiResponse.failure("Invalid credentials"));

        User user = userOpt.get();
        Token sessionToken = tokenService.createToken(user, Token.TokenType.SESSION, Utils.generateRandom(4), String.format("Created with User-Agent %s - IP Address - %s", useragent, request.getRemoteAddr()), (long) (30 * 60)); // change expiry later

        return ResponseEntity.ok(ResponsesDto.ApiResponse.success(new ResponsesDto.LoginResponse(sessionToken.getToken(), sessionToken.getExpiresAt())));
    }

    private String getParam(Map<String, String[]> params, String key) {
        return params.containsKey(key) ? params.get(key)[0] : null;
    }
}
