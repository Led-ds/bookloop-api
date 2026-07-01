package com.bookloop.user.domain;

import com.bookloop.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Aggregate root for an account holder. A user can be both Leitor (renter) and
 * Dono (owner) at the same time — the role is contextual, not a fixed type.
 */
@Getter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Column(length = 120)
    private String location;

    /** Cumulative count of confirmed penalties (late returns, damage, etc.). */
    @Column(name = "penalties_count", nullable = false)
    private int penaltiesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean active = true;

    private User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /** Factory: enforces invariants at creation time. */
    public static User register(String name, String email, String passwordHash) {
        return new User(name, email, passwordHash);
    }

    public void updateProfile(String name, String bio, String location, String avatarUrl) {
        if (name != null && !name.isBlank()) this.name = name;
        this.bio = bio;
        this.location = location;
        this.avatarUrl = avatarUrl;
    }

    public void addPenalty() {
        this.penaltiesCount++;
    }

    /** Business rule: heavily penalized users are blocked from new rentals. */
    public boolean canRequestRentals() {
        return active && penaltiesCount < 3;
    }
}
