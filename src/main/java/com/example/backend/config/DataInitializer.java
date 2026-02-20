package com.example.backend.config;

import com.example.backend.entity.Category;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserType;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeds default roles (USER, ADMIN) and an optional admin user for development.
 * Runs only when profile "dev" is active or when no profile is set (default).
 */
@Component
@RequiredArgsConstructor
//@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        createRolesIfMissing();
        createAdminUserIfMissing();
        createDefaultCategories();
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

    private void createDefaultCategories() {
        User user1 = userRepository.findById(1L).orElse(null);
        if (user1 == null) {
            // Create user with id 1 if it doesn't exist (admin user should have id 1 after creation)
            user1 = userRepository.findByEmail("admin@example.com").orElse(null);
            if (user1 == null) {
                return; // No user to assign categories to
            }
        }

        String[] categories = {
                "Church",
                "Venue",
                "Wedding rings 💍",
                "Ring box or pillow",
                "Wedding dress",
                "Shoes",
                "Suit or tuxedo",
                "Invitations",
                "Seating plan",
                "Catering (food & drinks)",
                "Wedding cake",
                "Music (DJ or band)",
                "Photographer / videographer",
                "Flowers",
                "Lighting"
        };

        for (String desc : categories) {
            if (categoryRepository.findByUser_IdOrderByCreatedAtDesc(user1.getId()).stream()
                    .noneMatch(c -> c.getDescription().equals(desc))) {
                Category category = new Category();
                category.setId(UUID.randomUUID().toString());
                category.setDescription(desc);
                category.setUser(user1);
                categoryRepository.save(category);
            }
        }
    }
}
