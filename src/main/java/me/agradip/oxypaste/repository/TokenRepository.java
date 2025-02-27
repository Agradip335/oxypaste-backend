package me.agradip.oxypaste.repository;

import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String token);
    List<Token> findByUser(User user);
    void deleteByUser(User user);
    void deleteByToken(String token);
}
