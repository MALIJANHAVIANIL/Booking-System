package com.booking.config;

import com.booking.entity.Reservation;
import com.booking.entity.ReservationStatus;
import com.booking.entity.Resource;
import com.booking.entity.Role;
import com.booking.entity.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build();

            User user = User.builder()
                    .username("Normal User")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(admin);
            userRepository.save(user);
        }

        if (resourceRepository.count() == 0) {
            Resource conferenceRoom = Resource.builder()
                    .name("Conference Room A")
                    .description("Large conference room with 4K projector and whiteboard")
                    .type("Meeting Room")
                    .location("Building 1, 2nd Floor")
                    .price(new BigDecimal("150.00"))
                    .available(true)
                    .build();

            Resource laptop = Resource.builder()
                    .name("MacBook Pro M2")
                    .description("16-inch M2 Max workstation laptop for developers")
                    .type("Equipment")
                    .location("IT Asset Locker")
                    .price(new BigDecimal("50.00"))
                    .available(true)
                    .build();

            Resource desk = Resource.builder()
                    .name("Hot Desk 101")
                    .description("Ergonomic standing desk with dual monitors")
                    .type("Workspace")
                    .location("Open Office Floor 3")
                    .price(new BigDecimal("25.00"))
                    .available(true)
                    .build();

            resourceRepository.save(conferenceRoom);
            resourceRepository.save(laptop);
            resourceRepository.save(desk);
        }

        if (reservationRepository.count() == 0) {
            User user = userRepository.findByEmail("user@example.com").orElse(null);
            User admin = userRepository.findByEmail("admin@example.com").orElse(null);
            Resource resource1 = resourceRepository.findById(1L).orElse(null);
            Resource resource2 = resourceRepository.findById(2L).orElse(null);

            if (user != null && resource1 != null) {
                // Reservation 1 -> Owned by user@example.com (USER)
                Reservation userReservation = Reservation.builder()
                        .user(user)
                        .resource(resource1)
                        .startTime(LocalDateTime.now().plusDays(1))
                        .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                        .price(resource1.getPrice())
                        .status(ReservationStatus.CONFIRMED)
                        .build();
                reservationRepository.save(userReservation);
            }

            if (admin != null && resource2 != null) {
                // Reservation 2 -> Owned by admin@example.com (ADMIN)
                Reservation adminReservation = Reservation.builder()
                        .user(admin)
                        .resource(resource2)
                        .startTime(LocalDateTime.now().plusDays(2))
                        .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                        .price(resource2.getPrice())
                        .status(ReservationStatus.PENDING)
                        .build();
                reservationRepository.save(adminReservation);
            }
        }
    }
}
