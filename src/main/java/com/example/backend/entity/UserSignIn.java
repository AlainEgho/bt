package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Tracks that a user has signed in. Last activity is refreshed on each authenticated request.
 * Rows with last_activity_at older than 1 hour are deleted by a scheduled job.
 */
@Entity
@Table(name = "user_sign_ins", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class UserSignIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "signed_in_at", nullable = false, updatable = false)
    private Instant signedInAt = Instant.now();

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (signedInAt == null) signedInAt = Instant.now();
        if (lastActivityAt == null) lastActivityAt = Instant.now();
    }
}
