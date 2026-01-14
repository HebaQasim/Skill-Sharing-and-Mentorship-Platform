package com.company.skillplatform.auth.config;

import com.company.skillplatform.auth.entity.Role;
import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.auth.repository.RoleRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.admin.email:admin@skillplatform.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.first-name:System}")
    private String adminFirstName;

    @Value("${app.bootstrap.admin.last-name:Admin}")
    private String adminLastName;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing system roles...");
        for (RoleName roleType : RoleName.values()) {
            roleRepository.findByName(roleType)
                    .orElseGet(() -> roleRepository.save(new Role(roleType)));
        }
        log.info("Role initialization completed.");

        if (!bootstrapEnabled) {
            log.info("Bootstrap disabled. Skipping admin creation.");
            return;
        }


        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                u -> log.info("Bootstrap admin already exists: {}", adminEmail),
                () -> createBootstrapAdmin()
        );
    }

    private void createBootstrapAdmin() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User admin = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email("admin@skillplatform.com")
                .password(passwordEncoder.encode("Admin123!"))
                .enabled(true)
                .department("SYSTEM")
                .jobTitle("Global Admin")
                .phoneNumber("0599959495")
                .build();



        admin.getRoles().add(adminRole);

        userRepository.save(admin);

        log.warn(" Bootstrap admin created: {}", adminEmail);
        log.warn(" Bootstrap admin password (dev only): {}", adminPassword);
        log.warn(" Please change the password after first login.");
    }
}
