package me.agradip.oxypaste.service;

import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String username, String email, String password, String creationIp) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);

        User user = new User(username, email, hashedPassword, salt, creationIp);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.filter(user -> BCrypt.checkpw(password, user.getPassword())).isPresent();
    }

    public boolean authenticateUserByUsername(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.filter(user -> BCrypt.checkpw(password, user.getPassword())).isPresent();
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
