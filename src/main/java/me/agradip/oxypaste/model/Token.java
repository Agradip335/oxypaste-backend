package me.agradip.oxypaste.model;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name = "tokens")
public class Token {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

//    @Enumerated(EnumType.ORDINAL) // Stores as 0 or 1 in DB
    @Column(nullable = false)
    private TokenType type;

    @Column(length = 64)
    private String name; // Only for API tokens

    @Column(length = 256)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant expiresAt; // Nullable for non-expiring tokens

    public Token() {
        this.createdAt = Instant.now();
    }

    public Token(User user, TokenType type, String name, String description, Instant expiresAt) {
        this.user = user;
        this.type = type;
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.token = generateToken(type, createdAt);
    }

    private static String generateToken(TokenType type, Instant createdAt) {
        long timestamp = createdAt.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        byte[] randomBytes = new byte[16]; // 16 random bytes
        RANDOM.nextBytes(randomBytes);
        String encodedRandom = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return type.getCode() + "." + timestamp + "." + encodedRandom;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public TokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }


    public enum TokenType {
        API(0),
        SESSION(1);

        private final int code;

        TokenType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TokenType fromCode(int code) {
            return switch (code) {
                case 0 -> API;
                case 1 -> SESSION;
                default -> throw new IllegalArgumentException("Invalid token type: " + code);
            };
        }
    }
}
