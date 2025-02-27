package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<Responses.ApiResponse<Responses.UserCreatedResponse>> createAccount(MultipartHttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        String username = getParam(params, "username");
        String email = getParam(params, "email");
        String password = getParam(params, "password");
        String creationIp = request.getRemoteAddr();

        if (username == null || email == null || password == null) return ResponseEntity.badRequest().body(Responses.ApiResponse.failure("Missing required fields"));
        if (userService.getUserByUsername(username).isPresent() || userService.getUserByEmail(email).isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body(Responses.ApiResponse.failure("An account with that username or email already exists"));

        User user = userService.registerUser(username, email, password, creationIp);
        return ResponseEntity.ok(Responses.ApiResponse.success(new Responses.UserCreatedResponse(user.getId(), user.getCreatedAt())));
    }

    private String getParam(Map<String, String[]> params, String key) {
        return params.containsKey(key) ? params.get(key)[0] : null;
    }
}
