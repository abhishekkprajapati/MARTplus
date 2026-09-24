package com.ShopSphere.config;

import com.ShopSphere.entity.User;
import com.ShopSphere.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            String adminEmail = "admin@shopsphere.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();

                admin.setFirstName("ShopSphere");
                admin.setLastName("Admin");
                admin.setEmail(adminEmail);
                admin.setPhone("9999999999");

                admin.setPassword(
                        passwordEncoder.encode("Admin@12345")
                );

                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");

                userRepository.save(admin);

                System.out.println(
                        "ShopSphere ADMIN account created successfully."
                );
            }
        };
    }
}
