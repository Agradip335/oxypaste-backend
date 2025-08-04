package me.agradip.oxypaste.service;

import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.UserRepository;
import me.agradip.oxypaste.security.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    @Value("${application.mail-registration}")
    private boolean mailRegistrationEnabled;

    public UserService(UserRepository userRepository, EmailService emailService, VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
    }

    public long countUsers() {
        return userRepository.count();
    }

    public String registerUser(String username, String email, String password, String creationIp) {
        User user = new User(username, email, password, null, creationIp);
        Map<String, Object> payload = generatePayloadFromObject(user);
        String token = verificationTokenService.signPayload(payload);

        if (mailRegistrationEnabled) {
            emailService.sendVerificationEmail(user, Instant.parse((String) payload.get("expInstant")), token);
        }

        return token;
    }

    public static Map<String, Object> generatePayloadFromObject(User user) {
        String salt = BCrypt.gensalt();
        String hash = BCrypt.hashpw(user.getPassword(), salt);
        Instant expiry = Instant.now().plus(Duration.ofHours(3));

        Map<String, Object> userData = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "password", hash,
                "salt", salt,
                "ip", user.getCreationIp()
        );

        return Map.of(
                "exp", expiry.getEpochSecond(),
                "expInstant", expiry.toString(), // Add ISO timestamp (for formatting later)
                "user", userData
        );
    }

    public User verifyAndRegisterFromToken(String token) {
        Map<String, Object> payload = verificationTokenService.verifyToken(token);
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) payload.get("user");

        String username = (String) userMap.get("username");
        String email = (String) userMap.get("email");
        String password = (String) userMap.get("password");
        String salt = (String) userMap.get("salt");
        String ip = (String) userMap.get("ip");

        if (userRepository.findByUsername(username).isPresent() ||
                userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Account already exists");
        }

        User user = new User(username, email, password, salt, ip);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID userId) {
        logger.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public boolean authenticateUser(String username, String password) {
        logger.debug("Authenticating user: {}", username);

        Optional<User> userOptional = userRepository.findByUsername(username);
        boolean authenticated = userOptional
                .filter(user -> BCrypt.checkpw(password, user.getPassword()))
                .isPresent();

        if (authenticated) {
            logger.debug("Authentication successful for user: {}", username);
        } else {
            logger.debug("Authentication failed for user: {}", username);
        }

        return authenticated;
    }

    public void deleteUser(UUID userId) {
        logger.debug("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
        logger.info("User deleted: {}", userId);
    }
}
