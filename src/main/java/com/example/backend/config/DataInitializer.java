package com.example.backend.config;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserType;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds default roles (USER, ADMIN) and an optional admin user for development.
 * Runs only when profile "dev" is active or when no profile is set (default).
 */
@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRolesIfMissing();
        createAdminUserIfMissing();
    }

    private void createRolesIfMissing() {
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(Role.RoleName.ROLE_USER));
        }
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(Role.RoleName.ROLE_ADMIN));
        }
    }

    private void createAdminUserIfMissing() {
        String adminEmail = "admin@example.com";
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow();
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUserType(UserType.INDIVIDUAL);
        admin.setEmailVerified(true);
        admin.getRoles().add(roleRepository.getReferenceById(adminRole.getId()));
        userRepository.save(admin);
    }
}
