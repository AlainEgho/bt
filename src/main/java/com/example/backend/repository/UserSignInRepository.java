package com.example.backend.repository;

import com.example.backend.entity.UserSignIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSignInRepository extends JpaRepository<UserSignIn, Long> {

    Optional<UserSignIn> findByUser_Id(Long userId);

    @Query("SELECT s FROM UserSignIn s WHERE s.lastActivityAt >= :since ORDER BY s.lastActivityAt DESC")
    List<UserSignIn> findAllActiveSince(@Param("since") Instant since);

    @Modifying
    @Query("DELETE FROM UserSignIn s WHERE s.lastActivityAt < :before")
    int deleteByLastActivityAtBefore(@Param("before") Instant before);
}
