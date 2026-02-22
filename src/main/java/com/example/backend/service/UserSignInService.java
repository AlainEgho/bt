package com.example.backend.service;

import com.example.backend.dto.OnlineUserDto;
import com.example.backend.entity.User;
import com.example.backend.entity.UserSignIn;
import com.example.backend.repository.UserSignInRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSignInService {

    private final UserSignInRepository userSignInRepository;
    private final UserRepository userRepository;

    @Value("${app.signin.inactivity-hours:1}")
    private int inactivityHours = 1;

    /** Call on login: create or update sign-in record. */
    @Transactional
    public void recordSignIn(Long userId) {
        userSignInRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        s -> {
                            s.setLastActivityAt(Instant.now());
                            userSignInRepository.save(s);
                        },
                        () -> {
                            User user = userRepository.findById(userId)
                                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
                            UserSignIn s = new UserSignIn();
                            s.setUser(user);
                            s.setSignedInAt(Instant.now());
                            s.setLastActivityAt(Instant.now());
                            userSignInRepository.save(s);
                        }
                );
    }

    /** Call on each authenticated request to keep session active. */
    @Transactional
    public void refreshActivity(Long userId) {
        userSignInRepository.findByUser_Id(userId).ifPresent(s -> {
            s.setLastActivityAt(Instant.now());
            userSignInRepository.save(s);
        });
    }

    /** Delete sign-in records with no activity for more than inactivity period. */
    @Scheduled(fixedDelayString = "${app.signin.cleanup-interval-ms:300000}") // default 5 min
    @Transactional
    public void deleteStaleSignIns() {
        Instant cutoff = Instant.now().minus(inactivityHours, ChronoUnit.HOURS);
        int deleted = userSignInRepository.deleteByLastActivityAtBefore(cutoff);
        if (deleted > 0) {
            log.debug("Removed {} stale sign-in record(s)", deleted);
        }
    }

    /** List users currently considered signed in (activity within inactivity window). */
    @Transactional(readOnly = true)
    public List<OnlineUserDto> listOnlineUsers(Long excludeUserId) {
        Instant since = Instant.now().minus(inactivityHours, ChronoUnit.HOURS);
        return userSignInRepository.findAllActiveSince(since).stream()
                .map(UserSignIn::getUser)
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .map(OnlineUserDto::fromUser)
                .toList();
    }
}
