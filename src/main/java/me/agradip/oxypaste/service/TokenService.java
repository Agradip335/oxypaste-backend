package me.agradip.oxypaste.service;

import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.Token.TokenType;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Value("${security.max-sessions}")
    private int maxActiveSessions;

    @Value("${security.max-api-tokens}")
    private int maxApiTokens;

    public int getMaxActiveSessions() {
        return maxActiveSessions;
    }

    public int getMaxApiTokens() {
        return maxApiTokens;
    }

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createToken(User user, TokenType type, String name, String description, Long duration) {
        Instant expiresAt = (duration != null) ? Instant.now().plusSeconds(duration) : null;
        Token token = new Token(user, type, name, description, expiresAt);
        return tokenRepository.save(token);
    }

    public Optional<Token> getToken(String tokenStr) {
        return tokenRepository.findByToken(tokenStr);
    }

    public List<Token> getTokensForUser(User user) {
        return new ArrayList<>(tokenRepository.findByUser(user));
    }

    public boolean isTokenValid(String tokenStr) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        return tokenOpt.isPresent() && (tokenOpt.get().getExpiresAt() == null || tokenOpt.get().getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public boolean revokeToken(User user, String tokenStr) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isPresent() && tokenOpt.get().getUser().equals(user)) {
            tokenRepository.delete(tokenOpt.get());
            return true;
        }
        return false;
    }

    @Transactional
    public void revokeAllTokens(User user) {
        tokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteExpiredTokens() {
        List<Token> expiredTokens = tokenRepository.findAll().stream()
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now()))
                .toList();

        tokenRepository.deleteAll(expiredTokens);
    }

    public Optional<User> getUserFromToken(String tokenStr) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        return tokenOpt.map(Token::getUser);
    }

    public int countActiveTokensForUser(UUID userId, TokenType type) {
        LocalDateTime now = LocalDateTime.now();
        return tokenRepository.countByUserIdAndTypeAndExpiresAtAfter(userId, type, now);
    }

    public boolean tokenNameExistsForUser(User user, String name) {
        return tokenRepository.existsByUserAndName(user, name);
    }
}