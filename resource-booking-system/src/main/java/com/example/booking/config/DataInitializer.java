package com.example.booking.config;

import com.example.booking.entity.*;
import com.example.booking.entity.Role;
import com.example.booking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UserRepository users, ResourceRepository resources,
                           PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            }

            if (users.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(encoder.encode("user123"));
                user.setRole(Role.USER);
                users.save(user);
            }

            if (resources.count() == 0) {
                Resource room = new Resource();
                room.setName("Conference Room A");
                room.setDescription("10-seat conference room");
                room.setActive(true);
                resources.save(room);

                Resource vehicle = new Resource();
                vehicle.setName("Company Vehicle");
                vehicle.setDescription("Standard booking vehicle");
                vehicle.setActive(true);
                resources.save(vehicle);
            }
        };
    }
}
