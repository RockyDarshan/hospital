package com.darshan.hospital.config;

import com.darshan.hospital.entity.Role;
import com.darshan.hospital.repository.UserRepository;
import com.darshan.hospital.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final UserAccountService accounts;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository users,
                       UserAccountService accounts,
                       @Value("${app.admin.email:}") String email,
                       @Value("${app.admin.password:}") String password) {
        this.users = users;
        this.accounts = accounts;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        if (email.isBlank() || password.length() < 8) {
            log.warn("No admin exists. Set ADMIN_EMAIL and ADMIN_PASSWORD "
                    + "(at least 8 characters) and restart.");
            return;
        }
        accounts.createAccount(email, password, Role.ADMIN);
        log.info("Created admin account: {}", email);
    }
}