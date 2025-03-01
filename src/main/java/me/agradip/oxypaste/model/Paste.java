package me.agradip.oxypaste.model;

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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_public")
    private boolean isPublic;

    public Paste() {
        this.id = generateRandomId(PASTE_ID_LEN);
        this.createdAt = LocalDateTime.now();
    }

    public Paste(String id) {
        this.id = id;
    }

    public Paste(String content, User user) {
        this.id = generateRandomId(PASTE_ID_LEN);
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.user = user;
    }

    public String getId() {
        return id;
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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic() {
        this.isPublic = true;
    }

    public void setPrivate() {
        this.isPublic = false;
    }

    private String generateRandomId(int len) {
        StringBuilder idBuilder = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = RANDOM.nextInt(ALLOWED_CHARS.length());
            idBuilder.append(ALLOWED_CHARS.charAt(index));
        }
        return idBuilder.toString();
    }
}
