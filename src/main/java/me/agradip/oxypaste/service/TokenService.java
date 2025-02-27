package me.agradip.oxypaste.service;

import me.agradip.oxypaste.model.Token;
import me.agradip.oxypaste.model.Token.TokenType;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createToken(User user, TokenType type, String name, String description, Long duration) {
        LocalDateTime expiresAt = (duration != null) ? LocalDateTime.now().plusSeconds(duration) : null;
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
        return tokenOpt.isPresent() && (tokenOpt.get().getExpiresAt() == null || tokenOpt.get().getExpiresAt().isAfter(LocalDateTime.now()));
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
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();

        tokenRepository.deleteAll(expiredTokens);
    }

    public Optional<User> getUserFromToken(String tokenStr) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(tokenStr);
        return tokenOpt.map(Token::getUser);
    }
}