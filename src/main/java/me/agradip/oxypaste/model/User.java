package me.agradip.oxypaste.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false, unique = true, columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String creationIp;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public User() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public User(String username, String email, String password, String salt, String creationIp) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.creationIp = creationIp;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public String getCreationIp() {
        return creationIp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
