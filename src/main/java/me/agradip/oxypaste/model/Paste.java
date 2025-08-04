package me.agradip.oxypaste.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Entity
@Table(name = "pastes")
public class Paste {

    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PASTE_ID_LEN = 8;
    private static final int DELETE_KEY_LEN = 16;

    @Id
    @Column(length = PASTE_ID_LEN, unique = true, nullable = false, updatable = false)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "visibility", nullable = false)
    private PasteVisibility visibility;

    @Column(name = "language")
    private Language language = Language.AUTO_DETECT;

    public Paste() {
        this.id = generateRandomId(PASTE_ID_LEN);
        this.createdAt = LocalDateTime.now();
        this.visibility = PasteVisibility.PRIVATE;
    }

    public Paste(String id) {
        this();

        this.id = id;
    }

    public Paste(String content, User user) {
        this();

        this.content = content;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PasteVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PasteVisibility visibility) {
        this.visibility = visibility;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    private String generateRandomId(int len) {
        StringBuilder idBuilder = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = RANDOM.nextInt(ALLOWED_CHARS.length());
            idBuilder.append(ALLOWED_CHARS.charAt(index));
        }
        return idBuilder.toString();
    }

    public enum PasteVisibility {
        PUBLIC, PRIVATE
    }

    public enum Language {
        AUTO_DETECT(""),
        PLAINTEXT("plaintext"),
        JAVASCRIPT("javascript"),
        CSS("css"),
        PYTHON("python"),
        TYPESCRIPT("typescript"),
        JAVA("java"),
        C("c"),
        CPP("cpp"),
        GO("go"),
        RUST("rs"),
        PHP("php"),
        RUBY("ruby"),
        BASH("bash"),
        JSON("json"),
        YAML("yaml"),
        MARKDOWN("markdown");

        private final String value;

        Language(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Language fromValue(@JsonProperty("language") String value) {
            if (value == null || value.isBlank()) {
                return AUTO_DETECT;
            }

            for (Language lang : values()) {
                if (lang.value.equalsIgnoreCase(value)) {
                    return lang;
                }
            }

            throw new IllegalArgumentException("Unknown language value: " + value);
        }

    }
}
