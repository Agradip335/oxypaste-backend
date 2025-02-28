package me.agradip.oxypaste.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(length = DELETE_KEY_LEN, nullable = false, name = "deletion_key")
    private String deletionKey;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "public")
    private boolean publicPaste;

    public Paste() {
        this.id = generateRandomId(PASTE_ID_LEN);
        this.createdAt = LocalDateTime.now();
        this.deletionKey = generateRandomId(DELETE_KEY_LEN);
    }

    public Paste(String id) {
        this.id = id;
    }

    public Paste(String content, User user) {
        this.id = generateRandomId(PASTE_ID_LEN);
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.deletionKey = generateRandomId(DELETE_KEY_LEN);
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

    public String getDeletionKey() {
        return deletionKey;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPublic() {
        return publicPaste;
    }

    public void setPublic() {
        this.publicPaste = true;
    }

    public void setPrivate() {
        this.publicPaste = false;
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
